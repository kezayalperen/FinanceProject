package com.xbank.bigdata.efthavale.producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileNotFoundException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new StringSerializer().getClass().getName());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, new StringSerializer().getClass().getName());

        DataGenerator dg = new DataGenerator();

        Producer producer = new KafkaProducer<String,String>(config);

        while (true){
            Thread.sleep(500);
            String data = dg.generate();
            ProducerRecord<String,String> rec = new ProducerRecord<String,String>("financeTopic3",data);
            System.out.println(data);
            producer.send(rec);
        }
    }
}
