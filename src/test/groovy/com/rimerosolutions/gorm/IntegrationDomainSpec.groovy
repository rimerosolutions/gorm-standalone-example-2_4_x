/*
 * Copyright 2015 Rimero Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rimerosolutions.gorm

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import com.rimerosolutions.gorm.domain.Person
import com.rimerosolutions.gorm.service.PersonService
import org.springframework.boot.SpringApplication
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.MessageSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

import spock.lang.Specification
import spock.lang.AutoCleanup
import spock.lang.Shared

/**
 * Integration tests
 *
 * @author Yves Zoundi
 */
class IntegrationDomainSpec extends Specification {

  private AnnotationConfigApplicationContext doLoad(Class config) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
    applicationContext.register(config)
    applicationContext.refresh()

    applicationContext
  }

  @EnableAutoConfiguration
  @ComponentScan
  private static class TestApplication {

    @Autowired
    private PersonService personService

    @Autowired
    private MessageSource messageSource
  }

  @Shared
  @AutoCleanup
  ApplicationContext context

  void setupSpec() {
    context = doLoad(TestApplication)
  }

  def 'We can save some valid domain objects'() {
    PersonService personService = context.getBean(PersonService)

    // Dummy user objects to persist
    given:
    def persons = [
      new Person("firstName":"Franscisco", "lastName":"DelaNoche"),
      new Person("firstName":"Emmanuel", "lastName":"Dupuit")
    ]

    when: 'When the person information is correct'
    persons.each { Person person -> 
      assert personService.validate(person)
    }

    then: 'We save the list of valid persons'
    persons.each { Person person ->
      personService.save(person)
    }

    expect: 'We should have 2 persons in the DB'
    def savedPersons = personService.findAll()
    println "savedPersons ${savedPersons}"
    savedPersons.size() == 2
  }
}
