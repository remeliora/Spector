<#import "parts/carcass.ftl" as carcas>
<#import "parts/authentication.ftl" as authentication>

<@carcas.page>
    <div class="mb-1">
        Add new user
    </div>
    ${message?ifExists}
    <@authentication.authentication "/registration" true/>
</@carcas.page>