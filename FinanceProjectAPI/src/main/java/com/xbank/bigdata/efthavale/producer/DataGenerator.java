package com.xbank.bigdata.efthavale.producer;

import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.*;

public class DataGenerator {

    public static List<String> names = new ArrayList<>();
    public static List<String> surnames = new ArrayList<>();
    public static Random r = new Random();
    public int processID = 10000;

    public DataGenerator() throws FileNotFoundException {
        File fileName = new File("C:\\Users\\Kezay\\Desktop\\isimler.txt");
        File fileSurname = new File("C:\\Users\\Kezay\\Desktop\\soyisimler.txt");

        Scanner fileNameScanner = new Scanner(fileName);
        Scanner fileSurnameScanner = new Scanner(fileSurname);


        while (fileNameScanner.hasNext()){
            names.add(fileNameScanner.nextLine());
        }

        while (fileSurnameScanner.hasNext()){
            surnames.add(fileSurnameScanner.nextLine());
        }
    }

    public String generate()  {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        int lowBalance = 5;
        int highBalance = 20000;

        List<String> bType = Arrays.asList("TL","EUR","USD");

        // Veriyi okuma
        //surnames.forEach(f -> System.out.println(f));

        int balance = r.nextInt(highBalance-lowBalance) + lowBalance;

        JSONObject data = new JSONObject();

        data.put("timestamp",timestamp.toString());
        data.put("balance",balance);
        data.put("bType",bType.get(r.nextInt(bType.size())));
        data.put("processID",processID++);
        data.put("processType","H");

        JSONObject account = new JSONObject();
        account.put("oID",generateID());
        account.put("title",generateNameSurname());
        account.put("iban","TR"+generateID());

        data.put("account",account);

        JSONObject info = new JSONObject();
        info.put("title",generateNameSurname());
        info.put("iban","TR"+generateID());
        info.put("bank","X bank");

        data.put("info",info);

        return data.toJSONString();
    }

    public static long generateID(){

        long numbers = 10000000000L + (long)(r.nextDouble()*9999999999L);

        return numbers;
    }

    public static String generateNameSurname(){

        String name = names.get(r.nextInt(names.size()));
        String surname = surnames.get(r.nextInt(surnames.size()));

        return name + " " + surname;
    }
}
