<!-- MIT License -->
# Reproducibility Bundle v1.1

1. `git clone <repo>` and `cd NginxLogAnalyzerJava`
2. Build the Flink job: `mvn -q package -DskipTests`
3. Start local services: `docker-compose up -d`
4. Install Python deps: `pip install -r data/requirements.txt`
5. Generate traffic with `python data/generate.py`
6. `helm repo add bitnami https://charts.bitnami.com/bitnami`
7. Deploy edge stack: `helm install edge bitnami/vector -f artifacts/helm/values-edge.yaml`
8. Deploy cloud stack: `helm install cloud bitnami/flink -f artifacts/helm/values-cloud.yaml` and `helm install kafka bitnami/kafka -f artifacts/helm/values-cloud.yaml` and `helm install ch bitnami/clickhouse -f artifacts/helm/values-cloud.yaml`
9. Port-forward Grafana: `kubectl port-forward svc/grafana 3000:80` and open `http://localhost:3000`
10. Run the load test: `TARGET=http://localhost ./bench/run_bench.sh`
