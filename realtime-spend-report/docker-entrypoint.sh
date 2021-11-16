#!/bin/bash
/opt/spark/dist/bin/spark-submit \
--class spark.streaming.playground.spend.SpendReport \
/opt/realtime-spend-report.jar