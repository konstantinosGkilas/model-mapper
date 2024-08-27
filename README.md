
# 🚀 Mapping Strategy Library

## 📝 Overview

The **Mapping Strategy Library** provides a robust solution for converting between entities and DTOs (Data Transfer Objects) in Java applications. This library leverages the power of `ModelMapper` to automate the mapping process while allowing for customizable handling of null values, validation of mapped objects, and the ability to patch DTOs with selective updates.

This library is designed to work seamlessly with Spring Boot, offering a simple and efficient way to manage object mapping in your projects.

## ✨ Key Features

- 🔄 **Automated Object Mapping:** Convert between entities and DTOs effortlessly using `ModelMapper`.
- 🎛️ **Customizable Null Handling:** Implement different strategies to handle null values in your DTOs.
- ✅ **Validation:** Automatically validate mapped objects to ensure data integrity.
- 🛠️ **Patching DTOs:** Update existing DTOs with new data while preserving certain fields, based on custom logic.
- 🏷️ **Annotation-based Configuration:** Easily integrate with Spring by marking your mappers with custom annotations.

## 🛠️ Problem Solved

In many enterprise applications, you often need to convert between different object types, such as entities and DTOs. This process can become repetitive and error-prone when handled manually. The **Mapping Strategy Library** addresses this issue by automating the conversion process, reducing boilerplate code, and ensuring consistency across your application.

Additionally, handling null values during mapping is a common challenge. This library allows you to define custom strategies for managing nulls, ensuring your business logic is correctly applied.

Another common requirement is to patch existing objects with partial updates. The library provides a mechanism to selectively update DTOs, which is useful for implementing PATCH HTTP methods in REST APIs.

## 💡 Why Use This Library?

- 🧹 **Reduces Boilerplate Code:** Simplifies the process of converting between entities and DTOs, allowing you to focus on business logic.
- 🌀 **Flexible Null Handling:** Choose how to handle null values in a way that best fits your application's needs.
- 🔍 **Built-in Validation:** Ensure that all mapped objects meet your validation requirements, reducing the likelihood of invalid data entering your system.
- 🛠️ **Spring Boot Integration:** Easily integrate with Spring Boot applications using annotation-based configuration.
- 🔄 **Extensible and Customizable:** While the library provides sensible defaults, you can extend and customize it to suit your specific requirements.

## 🚀 Getting Started

### 📋 Prerequisites

- ☕ **Java 17** or higher
- 🎯 **Spring Boot 3.3.2** or higher
- 🛠️ **Maven 3.8.1** or higher

### 📦 Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.kgkilas</groupId>
    <artifactId>mapping-strategy</artifactId>
    <version>0.0.1</version>
</dependency>
```

### ⚙️ Configuration

To configure the library, you need to extend `BaseMapperBeanConfig` in your Spring Boot application. For example:

```java
@Configuration(proxyBeanMethods = false)
public class CustomMapperBeanConfig extends BaseMapperBeanConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    public CustomMapperBeanConfig() {
        super("com.gkilas.filtering.rest.service");
    }
}
```

This configuration enables `ModelMapper` with a strict matching strategy, ensuring precise mapping between your entities and DTOs.

### 🔧 Usage

To create a custom mapper, extend the `GenericMapper` class and annotate your class with `@MapperBean`:

```java
@MapperBean
public class BookMapper extends GenericMapper<Book, BookDTO> {
    public BookMapper(ModelMapper modelMapper) {
        super(modelMapper, Book.class, BookDTO.class);
    }
}
```

Now, you can inject `BookMapper` into your services and start using it to convert between `Book` entities and `BookDTO` objects.

### 📝 Example

```java
@Service
public class BookService {

    private final BookMapper bookMapper;

    @Autowired
    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public BookDTO getBookDtoById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return bookMapper.toDTO(book);
    }

    public Book saveBook(BookDTO bookDTO) {
        Book book = bookMapper.toEntity(bookDTO);
        return bookRepository.save(book);
    }
}
```

### 🛠️ Maven Dependency Configuration

Make sure to add the required dependencies for the library in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.kgkilas</groupId>
        <artifactId>mapping-strategy</artifactId>
        <version>0.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>2.4.4</version>
    </dependency>
    <!-- Additional dependencies -->
</dependencies>
```

## 🎯 Conclusion

The **Mapping Strategy Library** is a powerful and flexible tool that streamlines the process of converting between entities and DTOs in Java applications. By reducing boilerplate code, enforcing validation, and providing customizable null handling strategies, this library can significantly improve your application's maintainability and reliability.

Whether you're working on a small project or a large enterprise application, the **Mapping Strategy Library** offers a clean and efficient solution to your object mapping needs. Give it a try, and enjoy the benefits of cleaner code and reduced development time.
