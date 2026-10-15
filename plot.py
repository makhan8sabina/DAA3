import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

# Создаем папку для сохранения графиков
os.makedirs("results/plots", exist_ok=True)

# Загрузка данных из CSV
csv_path = "results/results.csv"
if not os.path.exists(csv_path):
    print(f"Ошибка: файл {csv_path} не найден! Сначала запустите BenchmarkRunner.")
    exit(1)

df = pd.read_csv(csv_path)

# ---------------------------------------------------------
# 1. Operations vs n (Log-Log axes)
# ---------------------------------------------------------
plt.figure(figsize=(10, 6))
for (algo, graph_type), group in df.groupby(["algorithm", "graph"]):
    plt.loglog(
        group["n"],
        group["edge_scans"],
        marker="o",
        label=f"{algo} ({graph_type})"
    )

plt.xlabel("Number of Vertices (n)")
plt.ylabel("Edge Scans")
plt.title("1. Operations vs n (Log-Log Scale)")
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
plt.tight_layout()
plt.savefig("results/plots/ops_vs_n.png", dpi=300)
plt.close()

# ---------------------------------------------------------
# 2. Scans per edge (edge_scans / m vs n)
# ---------------------------------------------------------
plt.figure(figsize=(10, 6))
for (algo, graph_type), group in df.groupby(["algorithm", "graph"]):
    plt.semilogx(
        group["n"],
        group["edge_scans"] / group["m"],
        marker="o",
        label=f"{algo} ({graph_type})"
    )

plt.xlabel("Number of Vertices (n)")
plt.ylabel("Edge Scans / m")
plt.title("2. Scans per Edge vs n (Flat line = Linear Work)")
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend(bbox_to_anchor=(1.05, 1), loc="upper left")
plt.tight_layout()
plt.savefig("results/plots/scans_per_edge.png", dpi=300)
plt.close()

# ---------------------------------------------------------
# 3. Share of edges Kruskal examines
# ---------------------------------------------------------
plt.figure(figsize=(10, 6))
kruskal_df = df[df["algorithm"] == "Kruskal"]
for graph_type, group in kruskal_df.groupby("graph"):
    plt.semilogx(
        group["n"],
        group["edge_scans"] / group["m"],
        marker="s",
        linewidth=2,
        label=f"Kruskal ({graph_type})"
    )

plt.xlabel("Number of Vertices (n)")
plt.ylabel("Edge Scans / m")
plt.title("3. Share of Edges Kruskal Examines")
plt.grid(True, which="both", linestyle="--", alpha=0.5)
plt.legend()
plt.tight_layout()
plt.savefig("results/plots/kruskal_edge_share.png", dpi=300)
plt.close()

# ---------------------------------------------------------
# Slope Check (Расчет наклонов для отчета)
# ---------------------------------------------------------
print("=== Slope Check Results (for REPORT.md) ===")
for algo in df["algorithm"].unique():
    sub = df[(df["algorithm"] == algo) & (df["graph"] == "sparse")]
    if len(sub) > 1:
        # Линейная регрессия в логарифмических координатах
        slope, _ = np.polyfit(np.log10(sub["n"]), np.log10(sub["edge_scans"]), 1)
        print(f"Algorithm: {algo:<15} | Slope (sparse): {slope:.2f}")

print("\nГрафики успешно сохранены в папку results/plots/")