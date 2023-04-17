//package com.darianngo.discordBot.services.impl;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.darianngo.discordBot.dtos.UserDTO;
//import com.darianngo.discordBot.repositories.UserRepository;
//
//class EloServiceImplTest {
//
//	@InjectMocks
//	private EloServiceImpl eloService;
//
//	@Mock
//	private UserRepository userRepository;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//	}
//
//	@Test
//	void testUpdateElo() {
//		UserDTO user1 = new UserDTO("user1", null, null, 1500, null, null, null, null, null, null, null, null);
//		UserDTO user2 = new UserDTO("user2", null, null, 1500, null, null, null, null, null, null, null, null);
//		List<UserDTO> winningTeam = Arrays.asList(user1, user2);
//
//		UserDTO user3 = new UserDTO("user3", null, null, 1500, null, null, null, null, null, null, null, null);
//		UserDTO user4 = new UserDTO("user4", null, null, 1500, null, null, null, null, null, null, null, null);
//		List<UserDTO> losingTeam = Arrays.asList(user3, user4);
//
//		int winningScore = 21;
//		int losingScore = 15;
//
//		eloService.updateElo(winningTeam, losingTeam, winningScore, losingScore);
//
//		verify(userRepository, times(2)).save(any(UserDTO.class));
//	}
//}
