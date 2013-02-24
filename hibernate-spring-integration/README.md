# Data at Rest Encryption with Jasypt and Hibernate

_14 Jan 2010_

Data at rest encryption is a commonly important pattern in any enterprise application within which certain information must be protected when placed in a persisted state. Among the difficulties of building applications that support data at rest encryption are distinguishing encrypted data from unencrypted data at the application layer, and the algorithms needed to handle translating from one to the other. An application which is aware that at some points its data may be encrypted and at other points it may not violates the practice of separation of concern. The possibility that data must be encrypted at rest is irrelevant to application layer logic, which need not be concerned with such matters. This idea is encapsulated by the phrase _transparent data at rest encryption_.

Jasypt solves part of this problem by providing an easy to use encryption library which abstracts the often unnecessary cryptography background from implementation and integration concerns. Coupled with a cryptography API such as Bouncy Castle, Jasypt integrates smoothly into a Hibernate and Spring environment to provide transparent data at rest encryption.

In the following example, a simple domain will be used to demonstrate Jasypt and Hibernate integration in a Spring application. Data will be transparently encrypted when written to the database, and transparently decrypted when read from the database.

## Domain

The domain consists of a simple class called `Employee`. The `Employee` class contains a field, `ssn`, which stores an employee's social security number. Due to the sensitive nature of this sort of data, it is a candidate for data at rest encryption.

_Employee.java:_

```java
@Entity
@Table(name = "employee")
public class Employee {

  @Id
  @Column(name = "identifier")
  private String identifier;

  @Column(name = "name")
  private String name;

  /**
   * The employee's social security number, which is to be encrypted within
   * the database.
   */
  @Column(name = "ssn")
  @Type(type = "encryptedString")
  private String ssn;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSsn() {
    return ssn;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }
}
```

The `Employee` class is a basic value object class storing an employee's name and social security number, as well as an identifier. The `ssn` property is annotated with Hibernate's `@Type` annotation, which allows its designation as an encrypted string field. The declaration of this type will be in `package-info.java` below.

_package-info.java:_

```java
@TypeDefs( {
  @TypeDef(name = "encryptedString",
      typeClass = EncryptedStringType.class,
      parameters = {
        @Parameter(name = "encryptorRegisteredName",
            value = "strongHibernateStringEncryptor")
      })
})
```

`package-info.java` is a convenient place for declaring encrypted field type definitions, as it provides a single place for potentially many types of encrypted field type definitions and can be scanned easily by Spring.

_Repository.java:_

```java
public interface Repository {

  public Object retrieve(Class<?> entityClass, String identifier);

  public void store(Object entity);
}
```

The `Repository` interface specifies a basic data store, providing data retrieval and storage, and will be implemented using Hibernate.

_HibernateRepository.java:_

```java
public class HibernateRepository implements Repository {

  private SessionFactory sessionFactory;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Object retrieve(Class<?> entityClass, String identifier) {
    DetachedCriteria criteria = DetachedCriteria.forClass(entityClass).add(
        Restrictions.idEq(identifier));
    return criteria.getExecutableCriteria(
        sessionFactory.getCurrentSession()).uniqueResult();
  }

  @Override
  public void store(Object entity) {
    sessionFactory.getCurrentSession().save(entity);
  }
}
```

The `HibernateRepository` class implements the `Repository` interface using Hibernate's API.

That's it for our application code. All that remains is to test it.

## Testing

_TransactionalHibernateRepository.java:_

```java
@Transactional
public class TransactionalHibernateRepository implements Repository {

  private HibernateRepository hibernateRepository;

  public void setHibernateRepository(HibernateRepository hibernateRepository) {
    this.hibernateRepository = hibernateRepository;
  }

  @Override
  public Object retrieve(Class<?> entityClass, String identifier) {
    return hibernateRepository.retrieve(entityClass, identifier);
  }

  @Override
  public void store(Object entity) {
    hibernateRepository.store(entity);
  }
}
```

The `TransactionalHibernateRepository` class is a dumb implementation of the `Repository` interface which is annotated as `@Transactional` and uses an instance of `HibernateRepository`. This will make it easy to let Spring handle transaction management and Hibernate `Session` management.

_HibernateRepositoryTest.java:_

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HibernateRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private HibernatePBEStringEncryptor hibernateStringEncryptor;

  @Resource
  @Qualifier("transactionalHibernateRepository")
  private Repository repository;

  @Before
  public void cleanDatabase() {
    jdbcTemplate.execute("delete from employee");
  }

  @Test
  public void testSsnEncryption() {

    Employee employee = new Employee();
    employee.setIdentifier("emp1");
    employee.setName("Johnny McDoe");
    employee.setSsn("123-456-7890");

    repository.store(employee);

    String encSsn = jdbcTemplate.queryForObject(
        "select ssn from employee where identifier = '"
            + employee.getIdentifier() + "'", String.class);

    assertNotNull(encSsn);
    assertFalse(employee.getSsn().equals(encSsn));
    assertEquals(employee.getSsn(), hibernateStringEncryptor
        .decrypt(encSsn));
  }

  @Test
  public void testSsnDecryption() {

    jdbcTemplate
        .execute("insert into employee (identifier, name, ssn) values ('emp1', 'Johnny McDoe', '"
            + hibernateStringEncryptor.encrypt("123-456-7890")
            + "')");

    Employee employee = (Employee) repository.retrieve(Employee.class, "emp1");

    assertEquals("emp1", employee.getIdentifier());
    assertEquals("Johnny McDoe", employee.getName());
    assertEquals("123-456-7890", employee.getSsn());
  }
}
```

The `HibernateRepositoryTest` class runs two basic tests on the code: `testSsnEncryption()`, which ensures that data stored in the database is properly encrypted, and `testSsnDecryption()`, which ensures that data retrieved from the database is properly decrypted. An instance of `HibernatePBEStringEncryptor` is used to decrypt and verify transparently encrypted data, and is passed from the Spring configuration for this test case, shown below.

_HibernateRepositoryTest-context.xml:_

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tx="http://www.springframework.org/schema/tx"
  xmlns:util="http://www.springframework.org/schema/util"
  xsi:schemaLocation=" http://www.springframework.org/schema/beans 
  http://www.springframework.org/schema/beans/spring-beans-2.5.xsd 
  http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-2.5.xsd 
  http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">

  <tx:annotation-driven />

  <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <property name="dataSource" ref="dataSource" />
  </bean>

  <bean id="transactionManager"
    class="org.springframework.orm.hibernate3.HibernateTransactionManager">
    <property name="sessionFactory" ref="sessionFactory" />
  </bean>

  <bean id="hibernateRepository" class="com.earldouglas.dbencryption.HibernateRepository">
    <property name="sessionFactory" ref="sessionFactory" />
  </bean>

  <bean id="transactionalHibernateRepository"
    class="com.earldouglas.dbencryption.TransactionalHibernateRepository">
    <property name="hibernateRepository" ref="hibernateRepository" />
  </bean>

  <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
    <property name="driverClass" value="org.hsqldb.jdbcDriver" />
    <property name="jdbcUrl" value="jdbc:hsqldb:mem:db" />
    <property name="user" value="sa" />
    <property name="password" value="" />
  </bean>

  <bean id="sessionFactory"
    class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"
    init-method="createDatabaseSchema">
    <property name="dataSource" ref="dataSource" />
    <property name="packagesToScan" value="com.earldouglas.dbencryption" />
    <property name="annotatedPackages" value="com.earldouglas.dbencryption" />
    <property name="hibernateProperties">
      <util:map>
        <entry key="hibernate.dialect" value="org.hibernate.dialect.HSQLDialect" />
        <entry key="hibernate.show_sql" value="false" />
      </util:map>
    </property>
  </bean>

  <bean id="hibernateStringEncryptor"
    class="org.jasypt.hibernate.encryptor.HibernatePBEStringEncryptor">
    <property name="registeredName">
      <value>strongHibernateStringEncryptor</value>
    </property>
    <property name="algorithm">
      <value>PBEWITHSHA256AND128BITAES-CBC-BC</value>
    </property>
    <property name="password">
      <value>jasypt</value>
    </property>
    <property name="provider">
      <bean class="org.bouncycastle.jce.provider.BouncyCastleProvider" />
    </property>
  </bean>

</beans>
```

The Spring configuration contained in `HibernateRepositoryTest-context.xml` is relatively straightfoward; it sets up a `JdbcTemplate` for manual manipulation of the database, a transaction manager, instances of `HibernateRepository`, `TransactionalHibernateRepository`, a c3p0 `DataSource`, and a Hibernate `SessionFactory`, provided by Spring's `AnnotatedSessionFactoryBean`. Note in this bean the `packagesToScan` and `annotatedPackages` properties, which are used to pick up the `@Entity`-annotated `Employee` class as well as the `@TypeDef` declarations in `package-info.java`. Finally, a `HibernatePBEStringEncryptor` is instantiated using a standard algorithm, a registered name specified in the corresponding `@TypeDef` from `package-info.java`, and Bouncy Castle as the JCE provider.

That's all there is to it. This is a very simple example, and there are many options regarding encryption algorithms, encryptable data types other than strings, and more.

## References

* [Integrating Jasypt with Hibernate 3](http://www.jasypt.org/hibernate3.html)
* [Bouncy Castle](http://www.bouncycastle.org/)

