#!/usr/bin/env python3
import argparse
import json
import math
import subprocess
from dataclasses import dataclass


PAIRS = [("1", "2"), ("1", "3"), ("1", "4"), ("2", "3"), ("2", "4"), ("3", "4")]


def softmax(theta):
    m = max(theta)
    exps = [math.exp(t - m) for t in theta]
    s = sum(exps)
    return [e / s for e in exps]


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def run_feature_dump(file1, file2):
    out = subprocess.check_output(
        ["java", "FeatureDump", file1, file2],
        text=True,
    ).strip()
    return json.loads(out)


@dataclass
class Sample:
    pair: str
    x: list  # [kw, id, op, seq, len]
    y: float  # 0..1


def build_samples(directory, base, labels):
    samples = []
    for i, j in PAIRS:
        file1 = f"{directory}/{base}{i}.java"
        file2 = f"{directory}/{base}{j}.java"
        feats = run_feature_dump(file1, file2)
        x = [feats["kw"], feats["id"], feats["op"], feats["seq"], feats["len"]]
        pair_key = f"{i}-{j}"
        if pair_key not in labels:
            raise SystemExit(f"missing label for {pair_key}")
        y = float(labels[pair_key]) / 100.0
        samples.append(Sample(pair=pair_key, x=x, y=y))
    return samples


def build_samples_multi(labels_by_dataset):
    samples = []
    for dataset, labels in labels_by_dataset.items():
        directory = f"../TestCode/{dataset}"
        base = dataset
        for s in build_samples(directory, base, labels):
            samples.append(Sample(pair=f"{dataset}:{s.pair}", x=s.x, y=s.y))
    return samples


def loss_and_grad_theta(samples, theta):
    w = softmax(theta)  # N weights, sum=1
    n = len(samples)

    # grad_w = (2/n) * sum_k (pred - y) * x
    grad_w = [0.0 for _ in w]
    loss = 0.0
    for s in samples:
        pred = dot(w, s.x)
        err = pred - s.y
        loss += err * err
        for i in range(len(w)):
            grad_w[i] += err * s.x[i]
    loss /= n
    for i in range(len(w)):
        grad_w[i] *= (2.0 / n)

    # softmax chain rule:
    # dL/dtheta_i = w_i * (grad_w_i - sum_j grad_w_j * w_j)
    gw_dot_w = dot(grad_w, w)
    grad_theta = [w[i] * (grad_w[i] - gw_dot_w) for i in range(len(w))]
    return loss, grad_theta, w


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--labels", default="labels_stack.json")
    parser.add_argument("--lr", type=float, default=0.8)
    parser.add_argument("--steps", type=int, default=2000)
    args = parser.parse_args()

    with open(args.labels, "r", encoding="utf-8") as f:
        labels = json.load(f)

    if isinstance(labels, dict) and "Cross" in labels and isinstance(labels["Cross"], list):
        dataset_labels = {k: v for k, v in labels.items() if isinstance(v, dict)}
        samples = build_samples_multi(dataset_labels)
        for item in labels["Cross"]:
            file1 = item["a"]
            file2 = item["b"]
            y = float(item["y"]) / 100.0
            feats = run_feature_dump(file1, file2)
            x = [feats["kw"], feats["id"], feats["op"], feats["seq"], feats["len"]]
            samples.append(Sample(pair=f"Cross:{item.get('name', file1 + ' vs ' + file2)}", x=x, y=y))
    elif isinstance(labels, dict) and all(isinstance(v, dict) for v in labels.values()):
        samples = build_samples_multi(labels)
    else:
        samples = build_samples("../TestCode/Stack", "Stack", labels)

    theta = [0.0, 0.0, 0.0, 0.0, 0.0]  # start from uniform weights
    lr = args.lr
    best = None

    for step in range(1, args.steps + 1):
        loss, grad_theta, w = loss_and_grad_theta(samples, theta)
        theta = [t - lr * g for t, g in zip(theta, grad_theta)]

        # mild decay to stabilize
        if step % 400 == 0:
            lr *= 0.7

        if best is None or loss < best[0]:
            best = (loss, w, step)

    best_loss, best_w, best_step = best
    print(f"best_step={best_step} mse={best_loss:.8f}")
    print(f"weights kw/id/op/seq/len = {[round(x, 6) for x in best_w]}")

    # report per-pair fit
    for s in samples:
        pred = dot(best_w, s.x) * 100.0
        print(
            f"{s.pair}: pred={pred:6.2f}  target={s.y*100:6.2f}  "
            f"x=[{s.x[0]:.3f},{s.x[1]:.3f},{s.x[2]:.3f},{s.x[3]:.3f},{s.x[4]:.3f}]"
        )


if __name__ == "__main__":
    main()
