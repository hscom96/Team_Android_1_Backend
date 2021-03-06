package com.eroom.erooja.domain.repos;

import com.eroom.erooja.domain.enums.JobInterestType;
import com.eroom.erooja.domain.model.JobInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobInterestRepository extends JpaRepository<JobInterest, Long> {
    List<JobInterest> findJobInterestsByJobInterestType(JobInterestType jobInterestType);

    List<JobInterest> findJobInterestsByJobGroup_Id(Long id);

    JobInterest findJobInterestById(Long id);

}
