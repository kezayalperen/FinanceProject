package com.xbank.bigdata.efthavale.consumer;

import com.mongodb.spark.MongoSpark;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;


public class Application {
    public static void main(String[] args) throws StreamingQueryException {

        System.setProperty("hadoop.home.dir", "C:\\hadoop-common-2.2.0-bin-master");

        SparkSession sparkSession = SparkSession.builder()
                .master("local")
                .appName("EFT-Transfer ")
                .config("spark.mongodb.output.uri","mongodb://127.0.0.1/finance.fin")
                .getOrCreate();

        StructType accountSchema = new StructType()
                .add("iban",DataTypes.StringType)
                .add("oID",DataTypes.LongType)
                .add("title",DataTypes.StringType);

        StructType infoSchema = new StructType()
                .add("bank",DataTypes.StringType)
                .add("iban",DataTypes.StringType)
                .add("title",DataTypes.StringType);

        StructType schema = new StructType()
                .add("timestamp", DataTypes.StringType)
                .add("balance",DataTypes.IntegerType)
                .add("bType",DataTypes.StringType)
                .add("processID",DataTypes.IntegerType)
                .add("processType", DataTypes.StringType)
                .add("account",accountSchema)
                .add("info",infoSchema);

        Dataset<Row> loadDataSet = sparkSession.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "financeTopic3")
                .load()
                .selectExpr("CAST(value as STRING)");


        Dataset<Row> rawDataSet = loadDataSet.select(functions.from_json(loadDataSet.col("value"), schema).as("data")).select("data.*");

        /*
         BATCH ANALYSIS

        Dataset<Row> groupDataset = rawDataSet.groupBy("bType").sum("balance");

        Dataset<Row> transferDataSet = rawDataSet.filter(rawDataSet.col("processType").equalTo("H"));

        transferDataSet.groupBy(functions.window(transferDataSet.col("timestamp"),"5 minute"),
               transferDataSet.col("bType")).sum("balance").show();

         STREAM ANALYSIS

        Dataset<Row> transferDataSet = rawDataSet.filter(rawDataSet.col("processType").equalTo("H"));

        Dataset<Row> volumeDataSet = transferDataSet.groupBy(functions.window(transferDataSet.col("timestamp"),"5 minute"),
                transferDataSet.col("bType")).sum("balance");

        volumeDataSet.writeStream().outputMode("complete").format("console").start().awaitTermination();
        */


        // WRITING MONGODB
        Dataset<Row> transferDataSet = rawDataSet.filter(rawDataSet.col("processType").equalTo("H"));

        Dataset<Row> volumeDataSet = transferDataSet.groupBy(functions.window(transferDataSet.col("timestamp"),"1 minute"),
                transferDataSet.col("bType")).sum("balance");

        volumeDataSet.writeStream().outputMode("complete").foreachBatch(new VoidFunction2 <Dataset<Row>, Long>() {
            @Override
            public void call(Dataset<Row> rowDataset, Long aLong) throws Exception {
                MongoSpark.write(rowDataset).mode("append").save();
            }
        }).start().awaitTermination();

    }
}
