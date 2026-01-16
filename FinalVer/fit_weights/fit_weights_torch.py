#!/usr/bin/env python3
import argparse
import json
import subprocess
import warnings

warnings.filterwarnings("ignore", message="Failed to initialize NumPy")

import torch


PAIRS = [("1", "2"), ("1", "3"), ("1", "4"), ("2", "3"), ("2", "4"), ("3", "4")]


def _feat(a: str, b: str) -> list[float]:
    j = json.loads(subprocess.check_output(["java", "FeatureDump", a, b], text=True))
    return [j["kw"], j["id"], j["op"], j["seq"], j["len"]]


def _load_samples(labels: dict):
    xs, ys, names = [], [], []

    for ds, m in labels.items():
        if ds == "Cross" or not isinstance(m, dict):
            continue
        for i, j in PAIRS:
            names.append(f"{ds}:{i}-{j}")
            xs.append(_feat(f"../TestCode/{ds}/{ds}{i}.java", f"../TestCode/{ds}/{ds}{j}.java"))
            ys.append(float(m[f"{i}-{j}"]) / 100.0)

    for it in labels.get("Cross", []):
        names.append(f"Cross:{it.get('name','pair')}")
        xs.append(_feat(it["a"], it["b"]))
        ys.append(float(it["y"]) / 100.0)

    return names, xs, ys


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--labels", default="labels_all.json")
    ap.add_argument("--steps", type=int, default=6000)
    ap.add_argument("--lr", type=float, default=0.05)
    ap.add_argument("--verbose", action="store_true")
    args = ap.parse_args()

    labels = json.load(open(args.labels, "r", encoding="utf-8"))
    names, xs, ys = _load_samples(labels)

    X = torch.tensor(xs, dtype=torch.float32)
    y = torch.tensor(ys, dtype=torch.float32)

    theta = torch.zeros(X.shape[1], requires_grad=True)
    opt = torch.optim.Adam([theta], lr=args.lr)

    best_loss, best_w, best_step = 1e9, None, 0
    for step in range(1, args.steps + 1):
        opt.zero_grad(set_to_none=True)
        w = torch.softmax(theta, dim=0)
        loss = ((X.mul(w).sum(1) - y) ** 2).mean()
        loss.backward()
        opt.step()

        l = float(loss.detach())
        if l < best_loss:
            best_loss, best_w, best_step = l, w.detach().cpu(), step

    w = best_w.tolist()
    print(f"best_step={best_step} mse={best_loss:.8f}")
    print(f"weights kw/id/op/seq/len = {[round(v, 6) for v in w]}")

    if args.verbose:
        for name, x, t in zip(names, xs, ys):
            p = sum(wi * xi for wi, xi in zip(w, x)) * 100.0
            print(f"{name}: pred={p:6.2f} target={t*100:6.2f}")


if __name__ == "__main__":
    main()
