package project.backend.auth.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import project.backend.domain.member.dto.MemberResponse
import project.backend.domain.member.entity.Member
import project.backend.domain.member.entity.ProviderType

class MemberDetails(member: Member) : UserDetails {

    val id: Long = member.id!!
    val usernameValue: String = member.username
    val email: String? = member.email
    private val passwordValue: String? = member.password
    val nickname: String = member.nickname
    val provider: ProviderType = member.provider
    val profileImg: String = member.profileImage

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword(): String? = passwordValue

    override fun getUsername(): String = usernameValue

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    companion object {
        fun toResponse(memberDetails: MemberDetails): MemberResponse {
            return MemberResponse(
                id = memberDetails.id,
                username = memberDetails.username,
                email = memberDetails.email,
                nickname = memberDetails.nickname,
                provider = memberDetails.provider,
                profileImg = memberDetails.profileImg
            )
        }
    }
}