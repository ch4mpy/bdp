package nc.sgcb.labs.customer;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.sgcb.labs.customer.domain.Customer;
import nc.sgcb.labs.customer.jpa.CustomerRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

   @Component
   @RequiredArgsConstructor
   @Slf4j
   static class CustomerServiceInitializer implements ApplicationRunner {

        private final CustomerRepository customerRepo;

       @Override
       @Transactional
       public void run(ApplicationArguments args) throws Exception {

           customerRepo.deleteAll();

           customerRepo.saveAllAndFlush(List.of(
                   createRandomCustomer(),
                   john(),
                   sarah()));

           var found = customerRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    "h","h"
                   );

           for (var c : found) {
               log.info(c.toString());
           }
       }

       private Customer createRandomCustomer() {
          return Customer.builder()
                  .firstName(randomString())
                  .lastName(randomString())
                  .birthDate(LocalDate.of(1980, 7, 2))
                  .birthLocation(randomString())
                  .build();
       }

       private Customer john() {
           return Customer.builder()
                   .firstName("John")
                   .lastName("Deuf")
                   .birthDate(LocalDate.of(1980, 7, 2))
                   .birthLocation("Pirae")
                   .build();
       }

       private Customer sarah() {
           return Customer.builder()
                   .firstName("Sarah")
                   .lastName("Croche")
                   .birthDate(LocalDate.of(1980, 7, 2))
                   .birthLocation("Pirae")
                   .build();
       }

       private String randomString() {
           return UUID.randomUUID().toString();
       }

   }
}
