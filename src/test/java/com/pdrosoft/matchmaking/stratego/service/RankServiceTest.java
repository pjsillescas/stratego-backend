package com.pdrosoft.matchmaking.stratego.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pdrosoft.matchmaking.exception.MatchmakingValidationException;
import com.pdrosoft.matchmaking.stratego.enums.Rank;

@ExtendWith(MockitoExtension.class)
public class RankServiceTest {

	@InjectMocks
	private RankServiceImpl rankService;

	// Marshal
	private static class MarshalArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, 0), //
					Arguments.of(Rank.GENERAL, 1), //
					Arguments.of(Rank.COLONEL, 1), //
					Arguments.of(Rank.MAJOR, 1), //
					Arguments.of(Rank.CAPTAIN, 1), //
					Arguments.of(Rank.LIEUTENANT, 1), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = MarshalArguments.class)
	void testCompareWithMarshal(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.MARSHAL, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.MARSHAL, defender)).isEqualTo(result);
		}
	}

	// General
	private static class GeneralArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, 0), //
					Arguments.of(Rank.COLONEL, 1), //
					Arguments.of(Rank.MAJOR, 1), //
					Arguments.of(Rank.CAPTAIN, 1), //
					Arguments.of(Rank.LIEUTENANT, 1), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = GeneralArguments.class)
	void testCompareWithGeneral(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.GENERAL, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.GENERAL, defender)).isEqualTo(result);
		}
	}

	// Colonel
	private static class ColonelArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, 0), //
					Arguments.of(Rank.MAJOR, 1), //
					Arguments.of(Rank.CAPTAIN, 1), //
					Arguments.of(Rank.LIEUTENANT, 1), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = ColonelArguments.class)
	void testCompareWithColonel(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.COLONEL, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.COLONEL, defender)).isEqualTo(result);
		}
	}

	// Major
	private static class MajorArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, 0), //
					Arguments.of(Rank.CAPTAIN, 1), //
					Arguments.of(Rank.LIEUTENANT, 1), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = MajorArguments.class)
	void testCompareWithMajor(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.MAJOR, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.MAJOR, defender)).isEqualTo(result);
		}
	}

	// Captain
	private static class CaptainArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, 0), //
					Arguments.of(Rank.LIEUTENANT, 1), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = CaptainArguments.class)
	void testCompareWithCaptain(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.CAPTAIN, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.CAPTAIN, defender)).isEqualTo(result);
		}
	}

	// Lieutenant
	private static class LieutenantArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, -1), //
					Arguments.of(Rank.LIEUTENANT, 0), //
					Arguments.of(Rank.SERGEANT, 1), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = LieutenantArguments.class)
	void testCompareWithLieutenant(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.LIEUTENANT, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.LIEUTENANT, defender)).isEqualTo(result);
		}
	}

	// Sergeant
	private static class SergeantArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, -1), //
					Arguments.of(Rank.LIEUTENANT, -1), //
					Arguments.of(Rank.SERGEANT, 0), //
					Arguments.of(Rank.MINER, 1), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = SergeantArguments.class)
	void testCompareWithSergeant(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.SERGEANT, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.SERGEANT, defender)).isEqualTo(result);
		}
	}

	// Miner
	private static class MinerArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, -1), //
					Arguments.of(Rank.LIEUTENANT, -1), //
					Arguments.of(Rank.SERGEANT, -1), //
					Arguments.of(Rank.MINER, 0), //
					Arguments.of(Rank.SCOUT, 1), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, 1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = MinerArguments.class)
	void testCompareWithMiner(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.MINER, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.MINER, defender)).isEqualTo(result);
		}
	}

	// Scout
	private static class ScoutArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, -1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, -1), //
					Arguments.of(Rank.LIEUTENANT, -1), //
					Arguments.of(Rank.SERGEANT, -1), //
					Arguments.of(Rank.MINER, -1), //
					Arguments.of(Rank.SCOUT, 0), //
					Arguments.of(Rank.SPY, 1), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = ScoutArguments.class)
	void testCompareWithScout(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.SCOUT, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.SCOUT, defender)).isEqualTo(result);
		}
	}

	// Spy
	private static class SpyArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, 1), //
					Arguments.of(Rank.GENERAL, -1), //
					Arguments.of(Rank.COLONEL, -1), //
					Arguments.of(Rank.MAJOR, -1), //
					Arguments.of(Rank.CAPTAIN, -1), //
					Arguments.of(Rank.LIEUTENANT, -1), //
					Arguments.of(Rank.SERGEANT, -1), //
					Arguments.of(Rank.MINER, -1), //
					Arguments.of(Rank.SCOUT, -1), //
					Arguments.of(Rank.SPY, 0), //
					Arguments.of(Rank.BOMB, -1), //
					Arguments.of(Rank.FLAG, 1), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = SpyArguments.class)
	void testCompareWithSpy(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.SPY, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.SPY, defender)).isEqualTo(result);
		}
	}

	// Bomb
	private static class BombArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, 2), //
					Arguments.of(Rank.GENERAL, 2), //
					Arguments.of(Rank.COLONEL, 2), //
					Arguments.of(Rank.MAJOR, 2), //
					Arguments.of(Rank.CAPTAIN, 2), //
					Arguments.of(Rank.LIEUTENANT, 2), //
					Arguments.of(Rank.SERGEANT, 2), //
					Arguments.of(Rank.MINER, 2), //
					Arguments.of(Rank.SCOUT, 2), //
					Arguments.of(Rank.SPY, 2), //
					Arguments.of(Rank.BOMB, 2), //
					Arguments.of(Rank.FLAG, 2), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = BombArguments.class)
	void testCompareWithBomb(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.BOMB, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.BOMB, defender)).isEqualTo(result);
		}
	}

	// flag
	private static class FlagArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, 2), //
					Arguments.of(Rank.GENERAL, 2), //
					Arguments.of(Rank.COLONEL, 2), //
					Arguments.of(Rank.MAJOR, 2), //
					Arguments.of(Rank.CAPTAIN, 2), //
					Arguments.of(Rank.LIEUTENANT, 2), //
					Arguments.of(Rank.SERGEANT, 2), //
					Arguments.of(Rank.MINER, 2), //
					Arguments.of(Rank.SCOUT, 2), //
					Arguments.of(Rank.SPY, 2), //
					Arguments.of(Rank.BOMB, 2), //
					Arguments.of(Rank.FLAG, 2), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = FlagArguments.class)
	void testCompareWithFlag(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.FLAG, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.FLAG, defender)).isEqualTo(result);
		}
	}

	// disabled
	private static class DisabledArguments implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(//
					Arguments.of(Rank.MARSHAL, 2), //
					Arguments.of(Rank.GENERAL, 2), //
					Arguments.of(Rank.COLONEL, 2), //
					Arguments.of(Rank.MAJOR, 2), //
					Arguments.of(Rank.CAPTAIN, 2), //
					Arguments.of(Rank.LIEUTENANT, 2), //
					Arguments.of(Rank.SERGEANT, 2), //
					Arguments.of(Rank.MINER, 2), //
					Arguments.of(Rank.SCOUT, 2), //
					Arguments.of(Rank.SPY, 2), //
					Arguments.of(Rank.BOMB, 2), //
					Arguments.of(Rank.FLAG, 2), //
					Arguments.of(Rank.DISABLED, 2)//

			);
		}
	}

	@ParameterizedTest
	@ArgumentsSource(value = DisabledArguments.class)
	void testCompareWithDisabled(Rank defender, int result) {

		if (result == 2) {
			assertThatThrownBy(() -> rankService.compareRanks(Rank.DISABLED, defender))
					.isInstanceOf(MatchmakingValidationException.class).hasMessage("Invalid ranks compared");

		} else {
			assertThat(rankService.compareRanks(Rank.DISABLED, defender)).isEqualTo(result);
		}
	}

}
