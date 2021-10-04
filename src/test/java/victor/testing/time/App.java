package victor.testing.time;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication


@RequiredArgsConstructor
public class App implements CommandLineRunner {
   private
   @Autowired
   TimeBasedLogic timeBasedLogic;

   public static void main(String[] args) {
      SpringApplication.run(App.class, args);
   }

   @Override
   public void run(String... args) throws Exception {
      // calls happen from many places in existing code:
      timeBasedLogic.isFrequentBuyer(13);
      timeBasedLogic.isFrequentBuyer(13);
      timeBasedLogic.isFrequentBuyer(13);
   }
}