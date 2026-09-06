package com.robinloom.jpath.fixtures;

import java.util.List;
import java.util.Optional;

public final class TestData {

    public record Address(String city) {}
    public record Person(Address address, String name, int age) {}
    public record PersonWithMultipleAddresses(List<Address> address) {}
    public record PersonWithOptionalAddress(Optional<Address> address) {}
    public record Department(List<Address> addresses) {}
    public record Company(List<Department> departments) {}
    public record Party(List<Person> persons) {}
    public record Car(String brand, String model, boolean electric) {}
    public record CarOwner(List<Car> cars) {}
    public record Account(String owner, long balance) {}
    public record Bank(List<Account> accounts) {}
    public record Product(String name, double price) {}
    public record Shop(List<Product> products) {}
    public record Item(String name, double price, boolean available) {}
    public record Order(int id, List<Item> items) {}
    public record Store(List<Order> orders) {}

    public static final class PersonPOJO {
        private final String name;
        private final int age;

        public PersonPOJO(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    public static final class PartyPOJO {
        private final List<PersonPOJO> persons;

        public PartyPOJO(List<PersonPOJO> persons) {
            this.persons = persons;
        }

        public List<PersonPOJO> getPersons() {
            return persons;
        }
    }

    public static final class PersonWithPublicFields {
        public final String name;
        public final int age;

        public PersonWithPublicFields(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static final class PartyWithPublicFields {
        public final List<PersonWithPublicFields> persons;

        public PartyWithPublicFields(List<PersonWithPublicFields> persons) {
            this.persons = persons;
        }
    }

    private TestData() {
        throw new UnsupportedOperationException("Utility class");
    }
}
