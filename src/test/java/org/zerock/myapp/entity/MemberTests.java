package org.zerock.myapp.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.zerock.myapp.util.PersistenceUnits;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NoArgsConstructor

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class MemberTests {
	// 영속성단위(Persistece Unit)별로 단 한번만 생성해야 합니다.
	// 이 팩토리 객체가 생성될때, 사용된 영속성 단위 설정대로,
	// 데이터소스(=즉, 커넥션풀)도 함께 생성되기 때문에, 여러번 생성해서는
	// 안되고, 싱글톤으로 생성해서, 공유하게 만들어야 합니다.
	// 하지만, 팩토리에서 생산한 엔티티 관리자는 여러번 만들어도 괜찮습니다.
	// 그리고 엔티티 관리자 생성에는 거의 자원이 소모되지 않지만, 그래도
	// 엔티티 관리자도 엄연히 모든 엔티티를 관리하는 주체이기 때문에,
	// 다 사용되고 나면 반드시 close 해줘야 하는것은 팩토리와 마찬가지입니다.
	private EntityManagerFactory emf;
	private EntityManager em;
	
	
	
	@BeforeAll
	void beforeAll() {	// 1회성 전처리 : 팩토리 생성
		log.trace("beforeAll() invoked.");
		
		// 1st. test
		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.H2);
		// 2nd. test
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.ORACLE);
		// 3rd. test
//		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.MYSQL);
		
		assertNotNull(this.emf);
		log.info("\t+ this.emf: {}", this.emf);
		
		// ------------
		this.em = this.emf.createEntityManager();
		assert this.em != null;
		log.info("\t+ this.em: {}", this.em);
	} // beforeAll
	
	@AfterAll
	void afterAll() {	// 1회성 후처리: 팩토리 파괴
		log.trace("afterAll() invoked.");
				
		try { 
			// 1. 엔티티 관리자부터 먼저 파괴
			Objects.requireNonNull(this.em);
			
			this.em.close(); 
		} finally {
			// 2. 엔티티 관리자 팩토리 파괴
			Objects.requireNonNull(this.emf);
			
			if(this.emf.isOpen()) {
				this.emf.close();
			} // if		
		} // try-finally			
	} // afterAll
	
	
	
//	@Disabled
	@Order(1)
//	@Test
	@RepeatedTest(1)
	@DisplayName("1. createEntity")
	@Timeout(value=1L, unit=TimeUnit.MINUTES)
	void createEntity() {
		log.trace("createEntity() invoked.");
		
		// 새로운 엔티티 인스턴스 생성 -> 상태: Transient(=NEW, Un-managed)
		// 엔티티 관리자의 관리를 전혀 받지 않는 상태
		// 비유: 취업준비생
		
		EntityTransaction tx = this.em.getTransaction();
		
		try {
			tx.begin();
			
				Member transientMember = new Member();
				transientMember.setName("NAME");
				transientMember.setAge(23);
				
				// 최초등록일시 데이터 저장
//				transientMember.setCreateDate(new Date());
				
				// 상태변이 : TRANSIENT -> MANAGED
				// Callback : @PrePersist -> @PostPersist called back.
				this.em.persist(transientMember);
			
			tx.commit();
		} catch(Exception e) {
			tx.rollback();
			throw e;
		} // try-catch
	} // createEntity
	
	
	
//	@Disabled
	@Order(2)
//	@Test
	@RepeatedTest(1)
	@DisplayName("2. findMember")
	@Timeout(value=1L, unit=TimeUnit.MINUTES)
	void findMember() {
		log.trace("findMember() invoked.");
		
		// 해당 엔티티에 1:1로 매핑된 테이블에 저장되어 있엇던 튜플이,
		// 엔티티 관리자의 MANAGED 상태로 전환됩니다. 즉, :
		// 상태전이: 데이터베이스 테이블의 튜플 -> MANAGED
		// Callback: @PostLoad
		
		final long PK = 1L;
		Member foundMember = this.em.<Member>find(Member.class, PK);
		
		assertNotNull(foundMember);
		log.info("\t+ foundMember: {}", foundMember);
	} // findMember
	
	
	
//	@Disabled
	@Order(3)
//	@Test
	@RepeatedTest(1)
	@DisplayName("3. updateEntity")
	@Timeout(value=1L, unit=TimeUnit.MINUTES)
	void updateEntity() {
		log.trace("updateEntity() invoked.");
		
		// 찾아낸 엔티티 인스턴스 수정 -> 상태: MANAGED
		// 엔티티 관리자의 관리를 받는 상태에서, 엔티티가 수정되면
		// 트랜잭션이 종료(commit)될 때에, 엔티티 관리자가 적절한 싯점에
		// 수정SQL을 생성/실행합니다.
		// 비유: 사무실에서 일하고 있는 사원을 불러내서(loaded),
		//       사원의 정보를 수정하는 것과 같습니다.
		
		EntityTransaction tx = this.em.getTransaction();
		
		try {
			tx.begin();
			
				// Step1. em.find() 메소드로 수정할 엔티티를 찾아오고
				final Long PK = 1L;	// Auto-boxing
				Member foundMember = this.em.<Member>find(Member.class, PK);
			
				// Step2. Step1에서 찾은 엔티티(MANAGED)를 수정
				assertNotNull(foundMember);
				log.info("\t+ foundMember: {}", foundMember);
				
				// PK속성값은 절대로 수정하시면 안됩니다.!!!
				foundMember.setAge(100);
			
				// Step3. 트랜잭션이 정상종료(commit)될 때, 엔티티관리자가
				//		  수정된 엔티티 내역대로, 테이블의 해당 튜플 수정
				
				// 엔티티관리자 안에는, 1차/2차캐쉬, SQL저장소, Snapshot저장소
				// 위의 Step2 에서 찾아낸 엔티티는 Snapshot 저장소(원본) 보관
				// 수정된 엔티티는 1차 캐쉬에 저장. 엔티티 관리자는, 원본인
				// Snapshot저장소에 보관된 원본 엔티티와 1차 캐쉬에 저장된
				// 수정된 엔티티와 비교(모든필드값 비교)해서, 결국 수정된 필드
				// 만 찾아내서, 테이블의 행을 수정하는 UPDATE SQL문자를 생성/실행			
			tx.commit();
		} catch(Exception e) {
			tx.rollback();
			throw e;
		} // try-catch
	} // updateEntity
	
	
	
//	@Disabled
	@Order(4)
//	@Test
	@RepeatedTest(1)
	@DisplayName("4. removeEntity")
	@Timeout(value=1L, unit=TimeUnit.MINUTES)
	void removeEntity() {
		log.trace("removeEntity() invoked.");
		
		// 찾아낸 엔티티 인스턴스 삭제 -> 상태: REMOVED
		// 엔티티 관리자의 관리를 받는 상태(MANAGED)에서, 엔티티가 삭제되면
		// 트랜잭션이 종료(commit)될 때에, 엔티티 관리자가 적절한 싯점에
		// 삭제SQL을 생성/실행합니다.
		// 비유: 더이상 이 회사 더러워서 못다니겠다! 라는 사원이
		//       사직서 내고, 퇴사하는 것과 같다!
		
		EntityTransaction tx = this.em.getTransaction();
		
		try {
			tx.begin();
			
				// Step1. em.find() 메소드로 수정할 엔티티를 찾아오고
				final Long PK = 1L;	// Auto-boxing
				Member foundMember = this.em.<Member>find(Member.class, PK);
			
				// Step2. Step1에서 찾은 엔티티(MANAGED)를 수정
				assertNotNull(foundMember);
				log.info("\t+ foundMember: {}", foundMember);
				
				// Step3. Step2 에서 검증된 엔티티의 삭제 수행
				this.em.remove(foundMember);
				
			tx.commit();
		} catch(Exception e) {
			tx.rollback();
			throw e;
		} // try-catch
	} // removeEntity
	

} // end class
