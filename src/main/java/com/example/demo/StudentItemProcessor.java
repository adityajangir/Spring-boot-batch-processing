package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class StudentItemProcessor implements ItemProcessor<NoSqlDataModel, Student> {

    private static final Logger log = LoggerFactory.getLogger(StudentItemProcessor.class);

    @Override
    public Student process(NoSqlDataModel stud) {
        final String firstName = stud.getFirstName().toUpperCase();
        final String lastName = stud.getLastName().toUpperCase();
        final String course = stud.getCourse().toUpperCase();
        final int Id = stud.getId();
        final Student transformedStudent = new Student(Id, firstName, lastName, course);
//        System.out.println(transformedStudent);

//        log.info("Converting (" + stud + ") into (" + transformedStudent + ")");
        return transformedStudent;
    }
}


