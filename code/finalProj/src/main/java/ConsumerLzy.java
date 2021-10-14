

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.*;

public class ConsumerLzy {
    private static final String topic = "movie_rating_records";
    private static Consumer<String, String> consumer;
    private static Connection connection;
    private Configuration conf;
    public ConsumerLzy(){

    }

    public void consume() throws IOException {

        Properties props=new Properties();
        props.put("bootstrap.servers","vm:9092");
        props.put("group.id","final-consumer-group");
        props.put("enable.auto.commit","true");
        props.put("auto.commit.interval.ms","1000");
        props.put("session.timeout.ms","30000");
        props.put("auto.offset.reset","earliest");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumer=new KafkaConsumer<String,String>(props);

        Configuration conf= HBaseConfiguration.create();
        conf.set("hbase.rootdir","hdfs://vm:9000/hbase");
        conf.set("hbase.zookeeper.quorum","vm");
        conf.set("hbase.cluster.distributed","true");

        consumer.subscribe(Arrays.asList(topic));
        connection = ConnectionFactory.createConnection(conf);
        HTable userTable = (HTable) connection.getTable(TableName.valueOf("movie_records"));
        Scan scan=new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());
        ResultScanner rs=userTable.getScanner(scan);
        int count = 0 ;
        for(Result result:rs)
        {
            count+=result.size();
        }

//        Thread countThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    long rowCount=0;
//                    try{
//                        TableName name=TableName.valueOf("movie_records");
//                        HTable table= (HTable) connection.getTable(name);
//                        Scan scan=new Scan();
//                        scan.setFilter(new FirstKeyOnlyFilter());
//                        ResultScanner rs=table.getScanner(scan);
//                        for(Result result:rs)
//                        {
//                            rowCount+=result.size();
//                        }
//                        System.out.println("RowCount:"+rowCount);
//                    }
//                    catch(Throwable e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        countThread.start();



        while(true){
            ConsumerRecords<String, String> records = consumer.poll(100);

            for(ConsumerRecord<String, String> record : records){
                String[] splits = record.value().split(",");
                Put put = new Put(("0000" + String.valueOf(++count)).getBytes());
                System.out.println("0000" + String.valueOf(count));
                if(record.key().contains("details")){
                    put.addColumn("details".getBytes(), "userId".getBytes(), splits[0].getBytes());
                    put.addColumn("details".getBytes(), "movieId".getBytes(), splits[1].getBytes());
                    put.addColumn("details".getBytes(), "rating".getBytes(), splits[2].getBytes());
                    put.addColumn("details".getBytes(), "timestamp".getBytes(), splits[3].getBytes());
                    System.out.println("details:");
                    for (String s : splits){
                        System.out.println(s);
                    }
                    userTable.put(put);
                }
                else{
                }
                System.out.println();
                //userTable.put(datas);
            }
        }
        //userTable.close();
        //connection.close();
    }

    public static void main(String[] args) throws IOException {
        new ConsumerLzy().consume();
    }


}


