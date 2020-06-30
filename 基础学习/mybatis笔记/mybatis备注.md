## 返回结果里面包含collection

```xml
	<resultMap id="result" type="com.xxx.xxx">
        <id column="ID" jdbcType="BIGINT" property="id"/>
        <result column="COL_NAME" jdbcType="VARCHAR" property="colName"/>
    	
        <!-- 这里的column值传递到queryList的查询参数-->	
        <collection property="COL_LIST" 
                    javaType="ArrayList" 
                    ofType="xxx.JavaType"
                    column="SELECTED_ID" 
                    select="com.xxx.xxxMapper.queryList"/>
    </resultMap>


	<select id="queryList">
        select xxx from XXX where id = #{selectedId}
	</select>
```

这里的column可以带更多的参数：

```xml
		<collection property="info"
					column="{colA=col_a,colB=col_b}"
					select="com.xxx.xxxMapper.queryList">
		</collection>
```

