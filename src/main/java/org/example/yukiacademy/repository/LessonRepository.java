package org.example.yukiacademy.repository;

import org.example.yukiacademy.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Modifying
    @Query("DELETE FROM Lesson l WHERE l.section.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}