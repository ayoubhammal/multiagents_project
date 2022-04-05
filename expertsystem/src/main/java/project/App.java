package project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {

    public class BaseResponse {
        String base;
        ArrayList<String> basesList;
        public BaseResponse(String base, Set<String> basesList) {
            this.base = base;
            this.basesList = new ArrayList<String>(basesList);
        }
        public String getbase() {
            return this.base;
        }
        public ArrayList<String> getBasesList() {
            return this.basesList;
        }
        
        @Override
        public String toString() {
            StringBuilder list = new StringBuilder();
            for (int i = 0; i < basesList.size(); ++i) {
                list.append("\"" + basesList.get(i) + "\"");
                if (i < basesList.size() - 1)
                    list.append(",");
            }

            return "{" +
                "\"base\" : " + this.base + "," + 
                "\"bases list\" : [" + list.toString() + 
                "]" +
            "}";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("api/bases")
    public String getBase(@RequestParam(name = "base", defaultValue = "base") String base) throws Exception {
        Scanner scanner = new Scanner(new File(this.getClass().getResource("/bases/" + base + ".json").getFile()));
        StringBuilder baseJSON = new StringBuilder();
        while (scanner.hasNextLine()) 
            baseJSON.append(scanner.nextLine());
        scanner.close();

        Set<String> basesFileNames = Stream.of(new File(this.getClass().getResource("/bases").getFile()).listFiles())
            .filter(file -> !file.isDirectory())
            .map(File::getName)
            .collect(Collectors.toSet());

        return new BaseResponse(baseJSON.toString(), basesFileNames).toString();
    }
}
