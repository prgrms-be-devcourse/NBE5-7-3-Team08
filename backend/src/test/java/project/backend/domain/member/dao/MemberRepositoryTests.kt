package project.backend.domain.member.dao

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType

@DataJpaTest
class MemberRepositoryTests @Autowired constructor(
    val memberRepository: MemberRepository
){

    @Test
    fun `findByEmail 유효한 Member 반환 테스트`() {
        val member = Member(
            username = "member1",
            nickname = "member1Nickname",
            email = "member1@gmail.com",
            password = "1234",
            provider = ProviderType.GITHUB,
            profileImage = "defaultProfileImage"
        )
        memberRepository.save(member)
        val findMember = memberRepository.findByEmail("member1@gmail.com")
        assertEquals(member.username, findMember?.username)
    }

    @Test
    fun `findByEmail null값 반환 테스트`() {
        val found = memberRepository.findByEmail("null@example.com")
        assertNull(found)
    }

    @Test
    fun `findByUsername 유효한 Member 반환 테스트`() {
        val member = Member(
            username = "member1",
            nickname = "member1Nickname",
            email = "member1@gmail.com",
            password = "1234",
            provider = ProviderType.GITHUB,
            profileImage = "defaultProfileImage"
        )
        memberRepository.save(member)

        val found = memberRepository.findByUsername("member1")
        assertEquals(member.email, found?.email)
    }

    @Test
    fun `findByUsername null 반환 테스트`() {
        val found = memberRepository.findByEmail("null@example.com")
        assertNull(found)
    }

}