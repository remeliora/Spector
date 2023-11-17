<#assign known = SPRING_SECURITY_CONTEXT??>

<#if known>
    <#assign
        user = SPRING_SECURITY_CONTEXT.authentication.principal
        name = user.getUsername()
        isAdmin = user.isAdmin()
    >
    <#else>
    <#assign
        name = "Неавторизованный пользователь"
        isAdmin = false
    >
</#if>