package project.backend.domain.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberStatus {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "member_id")
	private Member member;

	@Setter
	private Long roomId;

	public MemberStatus(Member member) {
		this.member = member;
	}
}
