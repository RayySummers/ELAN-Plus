package mpi.eudico.server.corpora.clomimpl.type;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinguisticTypeTest {
	private LinguisticType lt1;
	private LinguisticType lt2;

	@BeforeEach
	void setUpBefore() throws Exception {
		lt1 = new LinguisticType("type1");
		lt2 = new LinguisticType("type2");
		lt2.addConstraint(new SymbolicAssociation());
		lt2.setTimeAlignable(false);
	}

	@DisplayName("The time alignable flag is currently set independently of or in addition to the constraints")
	@Test
	void testIsTimeAlignable() {
		assertTrue(lt1.isTimeAlignable());
		assertFalse(lt2.isTimeAlignable());
	}

	@DisplayName("After overriding hashCode() the type should not be used as key in a hash map or table if it can also be modified")
	@Test
	void testMapKey() {
		Map<LinguisticType, Object> typeMap = new HashMap<>(2);
		typeMap.put(lt1, "one");
		typeMap.put(lt2, "two");
		lt1.setControlledVocabularyName("CV1");
		assertNull(typeMap.get(lt1), "The value for a modified linguistic type as key can not be retrieved (with get()) anymore");
		assertNotNull(typeMap.get(lt2), "The value for a unmodified linguistic type as key can be retrieved");
	}
	
	@DisplayName("A type can be modified when it is in a list and its index can still be retrieved")
	@Test
	void testListIndex() {
		List<LinguisticType> typeList = new ArrayList<>(2);
		typeList.add(lt1);
		typeList.add(lt2);
		lt2.setLinguisticTypeName("type3");
		assertTrue(typeList.indexOf(lt2) == 1, "The type can still be retrieved from an arraylist after modification");
	}
}
