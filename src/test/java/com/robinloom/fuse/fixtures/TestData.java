package com.robinloom.fuse.fixtures;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class TestData {

    public record Address(String city) {}
    public record Person(Address address, String name, int age) {}
    public record PersonWithMultipleAddresses(List<Address> address) {}
    public record PersonWithOptionalAddress(Optional<Address> address) {}
    public record PartyWithOptionalAddresses(List<PersonWithOptionalAddress> persons) {}
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
    public record Deal(String name, double salePrice, double listPrice) {}
    public record Catalog(List<Deal> deals) {}
    public record ItemWithOptionalPrice(String name, Optional<Double> price) {}
    public record Cart(List<ItemWithOptionalPrice> items) {}
    public record Employee(String name, double salary) {}
    public record Team(String name, Optional<List<Employee>> members) {}
    public record Metric(OptionalInt quantity, OptionalLong total, OptionalDouble average) {}
    public record Metrics(List<Metric> metrics) {}

    public record PersonPOJO(String name, int age) {
    }

    public record PartyPOJO(List<PersonPOJO> persons) {
    }

    public record PersonWithPublicFields(String name, int age) {
    }

    public record PartyWithPublicFields(List<PersonWithPublicFields> persons) {
    }

    private TestData() {
        throw new UnsupportedOperationException("Utility class");
    }
}
