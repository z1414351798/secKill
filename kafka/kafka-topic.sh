docker exec -it kafka1 /bin/bash
[appuser@kafka1 ~]$ kafka-topics --create --topic inventory-deduct-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
Created topic inventory-deduct-topic.
[appuser@kafka1 ~]$ kafka-topics --create --topic payment-result-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
Created topic payment-result-topic.
kafka-topics --create --topic inventory-deduct-result-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
kafka-topics --create --topic refund-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
kafka-topics --create --topic refund-result-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3

kafka-topics --create --topic inventory-deduct-dead-letter-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
kafka-topics --create --topic inventory-rollback-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
kafka-topics --create --topic inventory-rollback-dead-letter-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3
kafka-topics --create --topic inventory-rollback-result-topic --bootstrap-server kafka1:19092 --partitions 6 --replication-factor 3


