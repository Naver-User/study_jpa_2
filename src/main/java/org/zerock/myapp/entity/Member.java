package org.zerock.myapp.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;	// JODA Time
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.zerock.myapp.listener.CommonEntityLifecyleListener;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Data

@EntityListeners(CommonEntityLifecyleListener.class)

@Entity(name="User")
@Table(name="Users")

@SequenceGenerator(name = "id_generator", sequenceName = "seq_users")
//@SequenceGenerator(
//		name = "id_generator",
//		sequenceName = "seq_users",
//		initialValue = 1, 
//		allocationSize = 1)
public class Member implements Serializable {
	@Serial private static final long serialVersionUID = 1L;

	// 1. PK 속성 선언
	@Id
	@GeneratedValue(
		strategy = GenerationType.SEQUENCE, 
		generator = "id_generator")
	private Long id;
	
	// 2. 일반속성 선언
	@Column(nullable = false)	// NOT NULL
	private String name;
	
//	@Basic(optional = true)		// NULL
	@Basic(optional = false)	// NOT NULL
	private Integer age;
	
	// *중요사항*: 정보통신망법에 따라, 중요한 개인정보나 영업정보/사업정보 등을
	//             저장하는 테이블에는 반드시 아래의 2개의 추가적인 컬럼을 가지도록
	//			   법제화 되어 있습니다:
	//				(1) 최초등록일시 - 한 개의 레코드가 최초로 등록된 일시저장
	//				(2) 최종수정일시 - 한 개의 레코드가 가장 마지막으로 수정된 일시저장
	
	@CreationTimestamp
	@Basic(optional = false)		// NOT NULL
	private Date createDate;	// 위 법제화된 (1) 항목 저장
	
	@UpdateTimestamp
	@Basic(optional = true)	// NULL
	private LocalDateTime lastModifiedDate;	// 위 법제화된 (2) 항목 저장
	
	
	//----------------
	// 각각의 엔티티 인스턴스의 생명주기에 따른,
	// 상태의 변화별로 자동 callback 되는 콜백 메소드를
	// JPA가 제공하는 아래와 같은 어노테이션을 통해서
	// 선언하시면, 엔티티관리자가 알아서 호출하게 됩니다:
	
	// (1) @PostLoad
	// (2) @PrePersist
	//     @PostPersist
	// (3) @PreUpdate
	//     @PostUpdate
	// (4) @PreRemove
	//     @PostRemove
	
	// * 위 각각이 어노테이션을 붙여서 만드는 각 메소드는
	//   가. 매개변수도 없고 나. 리턴타입도 없게 만듭니다.

	/**
	@PostLoad
	void postLoad() {
		log.trace("1. postLoad() invoked.");
	} // postLoad
	
	@PrePersist
	void prePersist() {
		log.trace("2. prePersist() invoked.");
	} // PrePersist
	
	@PostPersist
	void postPersist() {
		log.trace("3. postPersist() invoked.");
	} // postLoad
	
	@PreUpdate
	void preUpdate() {
		log.trace("4. preUpdate() invoked.");
	} // PreUpdate
	
	@PostUpdate
	void postUpdate() {
		log.trace("5. postUpdate() invoked.");
	} // PostUpdate
	
	@PreRemove
	void preRemove() {
		log.trace("6. preRemove() invoked.");
	} // PreRemove
	
	@PostRemove
	void postRemove() {
		log.trace("7. postRemove() invoked.");
	} // postRemove
	*/
	
   
} // end class



