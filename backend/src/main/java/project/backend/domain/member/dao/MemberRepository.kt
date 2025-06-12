package project.backend.domain.member.dao

import org.springframework.data.jpa.repository.JpaRepository
import project.backend.domain.member.entity.Member
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Member?

    fun findByUsername(username: String): Member?
}