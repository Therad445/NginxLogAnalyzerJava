
## Prerequisites
- Docker 20.10+
- Helm 3.10+
- k6 0.48+
- Java 17+
- Maven 3.9+
- Python 3.10+

## Quick Start
1. `git clone <repo>` and `cd NginxLogAnalyzerJava`
2. Build the Flink job: `mvn -q package -DskipTests`
3. Start local services: `docker compose up -d`
4. Install Python deps: `pip install -r data/requirements.txt`
5. Generate traffic: `python data/generate.py`
6. `helm repo add bitnami https://charts.bitnami.com/bitnami`
7. Deploy edge: `helm install edge bitnami/vector -f artifacts/helm/values-edge.yaml`
8. Deploy Kafka: `helm install kafka bitnami/kafka -f artifacts/helm/values-kafka.yaml`
9. Deploy Flink and ClickHouse:
   `helm install flink bitnami/flink -f artifacts/helm/values-flink.yaml`
   `helm install ch bitnami/clickhouse -f artifacts/helm/values-clickhouse.yaml`
10. Port-forward Grafana: `kubectl port-forward svc/grafana 3000:80 &`
    Run the load test: `TARGET=http://localhost ./bench/run_bench.sh`
