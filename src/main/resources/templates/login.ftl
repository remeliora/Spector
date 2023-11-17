<#import "parts/carcass.ftl" as carcas>
<#import "parts/authentication.ftl" as authentication>

<@carcas.page>
    ${message?ifExists}
    <@authentication.authentication "/login" false/>
</@carcas.page>