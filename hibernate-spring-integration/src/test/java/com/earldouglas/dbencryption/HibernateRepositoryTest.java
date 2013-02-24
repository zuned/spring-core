package com.earldouglas.dbencryption;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

import java.util.Map;

import javax.annotation.Resource;

import org.jasypt.hibernate.encryptor.HibernatePBEStringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
