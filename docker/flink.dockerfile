FROM flink:1.17.0-scala_2.12-java11
RUN wget -P /opt/flink/lib https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/1.17.0/flink-sql-connector-kafka-1.17.0.jar