# loganalytics-log4j-appender
Log4j 1.2 appender for pushing logs to Azure Log Analytics

Example configuration in log4j.xml:

```xml
...
<appender name="loganalytics" class="com.dionoid.log4j.LogAnalyticsAppender">
    <!-- note: the log Type will get a '_CL' suffix in Log Analytics to distinguish it as a Custom Log -->
    <param name="LogType" value="Test" />
    <param name="WorkspaceId" value="[Your OMS WorkspaceId here!]" />
    <param name="SharedKey" value="[Your OMS Primary or Secondary Key here!]" />
</appender>
...
```

For best performance, wrap the LogAnalyticsAppender inside an AsyncAppender, so logging won't hold up execution of your code:

```xml
...
<appender name="async" class="org.apache.log4j.AsyncAppender">
    <appender-ref ref="loganalytics"/>
</appender>
...
...
<root>
    <level value="INFO" />
    <appender-ref ref="async"/>
</root>
...
```
