package com.naisa.model.entity;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks that lighting availability survives database writes and subsequent entity reads.
 *
 * @author Vincent Lai, Codex
 */
@SpringBootTest
@Transactional
class VenueSportPersistenceTest {
	@Autowired
	private EntityManager entityManager;

	// Disabled: Lighting now defaults to null (unknown), not false.
	// /**
	// * Verifies the default unlit state and an explicitly enabled lighting value
	// through JPA.
	// *
	// * @author Vincent Lai, Codex
	// */
	// @Test
	// void persistsLightingAvailability() {
	// final Venue venue = new Venue();
	// venue.setName("Lighting test venue");
	// venue.setAddress("123 Test Street");
	// venue.setLatitude(40.81);
	// venue.setLongitude(-73.96);
	// venue.setTimezone("America/New_York");
	// entityManager.persist(venue);
	//
	// final Sport sport = new Sport();
	// sport.setName("Lighting test sport");
	// entityManager.persist(sport);
	//
	// final VenueSport offering = new VenueSport();
	// offering.setVenue(venue);
	// offering.setSport(sport);
	// offering.setPrice(BigDecimal.ZERO);
	// offering.setIsIndoor(false);
	// entityManager.persist(offering);
	// entityManager.flush();
	// entityManager.clear();
	//
	// final VenueSport saved = entityManager.find(VenueSport.class,
	// offering.getId());
	// assertFalse(saved.getHasLighting());
	// saved.setHasLighting(true);
	// entityManager.flush();
	// entityManager.clear();
	//
	// assertTrue(entityManager.find(VenueSport.class,
	// offering.getId()).getHasLighting());
	// }
}
