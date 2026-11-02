import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String CSV_DATA_FILE = "data.csv";
    private static final String JSON_DATA_FILE = "data.json";
    private static final String JSON_DATA_FILE2 = "data2.json";
    private static final String XNL_DATA_FILE = "data.xml";

    private static List<Employee> csvParce(String[] columnMapping, String fileName){
        List<Employee> list = null;
        try(CSVReader reader = new CSVReader(new FileReader(fileName))){
            ColumnPositionMappingStrategy<Employee> startegy = new ColumnPositionMappingStrategy<>();
            startegy.setType(Employee.class);
            startegy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(startegy)
                    .build();
            list = csv.parse();
        }catch(Exception e){
            System.out.println("Ошибка при чтении csv-файла <" + fileName + ">: " + e);
        }
        return list;
    }

    private static String listToJson(List<Employee> list){
        if(list == null || list.isEmpty()) return "";

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .setPrettyPrinting()
                        .create();
        Type type = new TypeToken<List<Employee>>(){}.getType();
        return gson.toJson(list, type);
    }

    private static void writeString(String fileName, String data){
        if(data == null || data.isEmpty() || fileName == null || fileName.isEmpty()) return;

        try(FileWriter writer = new FileWriter(fileName)){
            writer.write(data);
        }catch(Exception e){
            System.out.println("Ошибка при записи в файл <" + fileName + ">: " + e);
        }
    }

    private static List<Employee> parseXML(String fileName){
        List<Employee> list = new ArrayList<>();
        if(fileName == null && fileName.isEmpty()) return list;
        File file = new File(".",fileName);
        if(!file.exists()) return list;

        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            Node node = doc.getDocumentElement();// <staff>...</staff>
            NodeList employees = node.getChildNodes();
            for(int i = 0; i < employees.getLength(); i++){
                Node n = employees.item(i);
                if(Node.ELEMENT_NODE == n.getNodeType()){
                    Element e = (Element)n;
                    Employee emp = new Employee();
                    emp.id = Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent());
                    emp.firstName = e.getElementsByTagName("firstName").item(0).getTextContent();
                    emp.lastName = e.getElementsByTagName("lastName").item(0).getTextContent();
                    emp.country= e.getElementsByTagName("country").item(0).getTextContent();
                    emp.age = Integer.parseInt(e.getElementsByTagName("age").item(0).getTextContent());
                    list.add(emp);
                }
            }
        }catch(Exception e){
            System.out.println("Ошибка при чтении xml-файла <" + fileName + ">: " + e);
        }
        return list;
    }

    private static String readString(String fileName){
        if(fileName == null || fileName.isEmpty()) return "";
        File file = new File(".", fileName);
        if(!file.exists()) return "";

        StringBuilder fileText = new StringBuilder();
        String line;
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
            while((line = br.readLine()) != null) fileText.append(line);
        }catch(Exception e){
            System.out.println("Ошибка при чтении файла <" + fileName + ">: " + e);
        }
        return fileText.toString();
    }

    private static List<Employee> jsonToList(String json){
        List<Employee> list = new ArrayList<>();
        if(json == null || json.isEmpty()) return list;

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type type = new TypeToken<List<Employee>>(){}.getType();
        list = gson.fromJson(json, type);
        return list;
    }

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> employees = csvParce(columnMapping, CSV_DATA_FILE);
        System.out.println("Десериализация объектов Employee из CSV-файла <" + CSV_DATA_FILE + ">");
        System.out.println(employees);
        String json = listToJson(employees);
        System.out.println("Объекты Employee в формате JSON: \n" + json + "\n");
        writeString(JSON_DATA_FILE, json);

        List<Employee> employees2 = parseXML(XNL_DATA_FILE);
        System.out.println(employees2);
        String json2 = listToJson(employees2);
        writeString(JSON_DATA_FILE2, json2);

        String json3 = readString(JSON_DATA_FILE2);
        List<Employee> employees3 = jsonToList(json3);
        System.out.println("Парсинг JSON-строки:");
        System.out.println(employees3);

    }
}
