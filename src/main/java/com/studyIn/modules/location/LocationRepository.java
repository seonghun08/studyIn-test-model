package com.studyIn.modules.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface LocationRepository extends JpaRepository<Location, Long> {

    Location findByCityAndProvince(String cityName, String provinceName);
}
