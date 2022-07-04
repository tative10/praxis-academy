Get-WmiObject -Class .java –ComputerName localhost |

Select-Object -Property CSName,@{n=”Last Booted”;

e={[Management.ManagementDateTimeConverter]::ToDateTime($_.LastBootUpTime)}}