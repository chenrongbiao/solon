package org.noear.solon.extend.mybatis.integration;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.Props;
import org.noear.solon.core.VarHolder;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.core.util.ScanUtil;
import org.noear.solon.extend.mybatis.MybatisAdapter;
import org.noear.solon.extend.mybatis.tran.SolonManagedTransactionFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis 适配器默认实现
 *
 * @author noear
 * @since 1.1
 */
public class MybatisAdapterDefault implements MybatisAdapter {
    protected final BeanWrap dsWrap;
    protected final Props dsProps;

    protected Configuration config;
    protected SqlSessionFactory factory;
    protected List<String> mappers = new ArrayList<>();
    protected SqlSessionFactoryBuilder factoryBuilder;

    /**
     * 构建Sql工厂适配器，使用默认的 typeAliases 和 mappers 配置
     */
    protected MybatisAdapterDefault(BeanWrap dsWrap) {
        this(dsWrap, Solon.cfg().getProp("mybatis"));
    }

    /**
     * 构建Sql工厂适配器，使用属性配置
     */
    protected MybatisAdapterDefault(BeanWrap dsWrap, Props dsProps) {
        this.dsWrap = dsWrap;
        if (dsProps == null) {
            this.dsProps = new Props();
        } else {
            this.dsProps = dsProps;
        }

        this.factoryBuilder = new SqlSessionFactoryBuilder();

        DataSource dataSource = dsWrap.raw();
        String dataSourceId = "ds-" + (dsWrap.name() == null ? "" : dsWrap.name());

        TransactionFactory tf = new SolonManagedTransactionFactory();
        Environment environment = new Environment(dataSourceId, tf, dataSource);

        initConfiguration(environment);

        //加载插件（通过配置）
        for (Interceptor i : MybatisPluginManager.getInterceptors()) {
            config.addInterceptor(i);
        }

        //加载插件（通过Bean）
        dsWrap.context().beanOnloaded((ctx) -> {
            ctx.beanForeach(bw -> {
                if (bw.raw() instanceof Interceptor) {
                    config.addInterceptor(bw.raw());
                }
            });
        });

        //1.分发事件，推给扩展处理
        EventBus.push(config);

        //2.初始化（顺序不能乱）
        initDo();

        dsWrap.context().getWrapAsync(SqlSessionFactoryBuilder.class, bw -> {
            factoryBuilder = bw.raw();
        });
    }

    protected void initConfiguration(Environment environment) {
        config = new Configuration(environment);

        //for configuration section
        Props cfgProps = dsProps.getProp("configuration");
        if (cfgProps.size() > 0) {
            Utils.injectProperties(config, cfgProps);
        }
    }

    protected void initDo() {
        //for typeAliases & typeHandlers section
        dsProps.forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                String key = (String) k;
                String valStr = (String) v;

                if (key.startsWith("typeAliases[") || key.equals("typeAliases")) {
                    for (String val : valStr.split(",")) {
                        val = val.trim();
                        if (val.length() == 0) {
                            continue;
                        }

                        if (val.endsWith(".class")) {
                            //type class
                            Class<?> clz = Utils.loadClass(val.substring(0, val.length() - 6));
                            if (clz != null) {
                                getConfiguration().getTypeAliasRegistry().registerAlias(clz);
                            }
                        } else {
                            //package
                            getConfiguration().getTypeAliasRegistry().registerAliases(val);
                        }
                    }
                }

                if (key.startsWith("typeHandlers[") || key.equals("typeHandlers")) {
                    for (String val : valStr.split(",")) {
                        val = val.trim();
                        if (val.length() == 0) {
                            continue;
                        }

                        if (val.endsWith(".class")) {
                            //type class
                            Class<?> clz = Utils.loadClass(val.substring(0, val.length() - 6));
                            if (clz != null) {
                                getConfiguration().getTypeHandlerRegistry().register(clz);
                            }
                        } else {
                            //package
                            getConfiguration().getTypeHandlerRegistry().register(val);
                        }
                    }
                }
            }
        });

        //支持包名和xml
        //for mappers section
        dsProps.forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                String key = (String) k;
                String valStr = (String) v;

                if (key.startsWith("mappers[") || key.equals("mappers")) {
                    for (String val : valStr.split(",")) {
                        val = val.trim();
                        if (val.length() == 0) {
                            continue;
                        }

                        if (val.endsWith(".xml")) {
                            //mapper xml
                            if (val.endsWith("*.xml")) {
                                String dir = val.substring(0, val.length() - 6);
                                ScanUtil.scan(dir, n -> n.endsWith(".xml"))
                                        .forEach(uri -> {
                                            addMapperByXml(uri);
                                        });
                            } else {
                                addMapperByXml(val);
                            }

                            mappers.add(val);
                        } else if (val.endsWith(".class")) {
                            //mapper class
                            val = val.replace("/", ".");

                            Class<?> clz = Utils.loadClass(val.substring(0, val.length() - 6));
                            if (clz != null) {
                                getConfiguration().addMapper(clz);
                                mappers.add(val);
                            }
                        } else {
                            //package
                            getConfiguration().addMappers(val);
                            mappers.add(val);
                        }
                    }
                }
            }
        });

        if (mappers.size() == 0) {
            throw new IllegalStateException("Please add the mappers configuration!");
        }

        if (config.getMapperRegistry().getMappers().size() == 0) {
            throw new IllegalStateException("Please check the mappers configuration!");
        }

        //for plugins section
        List<Interceptor> interceptors = MybatisPluginUtils.resolve(dsProps, "plugins");
        for (Interceptor itp : interceptors) {
            getConfiguration().addInterceptor(itp);
        }
    }

    /**
     * 获取配置器
     */
    @Override
    public Configuration getConfiguration() {
        return config;
    }

    /**
     * 获取会话工厂
     */
    @Override
    public SqlSessionFactory getFactory() {
        if (factory == null) {
            factory = factoryBuilder.build(getConfiguration());//new SqlSessionFactoryProxy(factoryBuilder.build(config));
        }

        return factory;
    }

    @Override
    public void injectTo(VarHolder varH) {
        //@Db("db1") MybatisAdapter adapter;
        if (MybatisAdapter.class.isAssignableFrom(varH.getType())) {
            varH.setValue(this);
            return;
        }

        //@Db("db1") SqlSessionFactory factory;
        if (SqlSessionFactory.class.isAssignableFrom(varH.getType())) {
            varH.setValue(this.getFactory());
            return;
        }

        //@Db("db1") Configuration cfg;
        if (Configuration.class.isAssignableFrom(varH.getType())) {
            varH.setValue(this.getConfiguration());
            return;
        }

        //@Db("db1") UserMapper userMapper;
        if (varH.getType().isInterface()) {
            Object mapper = this.getMapper(varH.getType());

            varH.setValue(mapper);
            return;
        }
    }

    protected void addMapperByXml(String uri) {
        try {
            // resource 配置方式
            ErrorContext.instance().resource(uri);

            //读取mapper文件
            InputStream stream = Resources.getResourceAsStream(uri);

            //mapper映射文件都是通过XMLMapperBuilder解析
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(stream, getConfiguration(), uri, getConfiguration().getSqlFragments());
            mapperParser.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}