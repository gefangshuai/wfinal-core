package net.wincn.core;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import net.wincn.utils.StrUtils;
import net.wincn.utils.TableUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库操作工具类，需要实例化
 * Created by fangshuai on 2014-11-08-0008.
 */
public class DBKit<T extends Model> {

    private Model dao;
    private String tableName;
    private String pkName;
    private int pageSize = 5;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public DBKit(T clazz) {
        this.dao = clazz;
        TableBind tableBind = TableUtils.getTableBind(clazz);
        tableName = tableBind.tableName();
        pkName = tableBind.pkName();
    }

    /**
     * 查询所有的数据
     *
     * @return
     */
    public List<T> listRecord(String sort) {
        return dao.find(String.format("select * from %s order by " + pkName + " " + sort, tableName));
    }

    /**
     * 分页查询
     * @param pageNumber 页号
     * @return
     */
    public Page<T> pageRecord(int pageNumber) {
        return dao.paginate(pageNumber, pageSize, "select * ", String.format("from %s order by " + pkName + " desc", tableName));
    }

    /**
     * 分页查询
     * @param pageNumber 页号
     * @param sqlExceptSelect 条件查询语句（e: from table where id = 1）
     * @param paras
     * @return
     */
    public Page<T> pageRecord(int pageNumber, String sqlExceptSelect, Object... paras) {
        return dao.paginate(pageNumber, pageSize, "select * ", sqlExceptSelect, paras);
    }

    /**
     * 查询封装，带分页支持
     * @param pageNumber 页号
     * @param params 查询参数
     * @param sort 排序：asc or desc
     * @return
     */
    public Page<T> search(int pageNumber, Map<String, Object> params, String sort) {
        StringBuilder sb = new StringBuilder("from " + tableName + " where 1=1 ");
        List<Object> objects = new ArrayList<Object>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            Object value = params.get(key);

            /**
             * 不等于
             */
            if (key.startsWith("ne_")) {
                key = StringUtils.remove(key, "ne_");
                sb.append("and " + key + " != ? ");
                objects.add(value);
            } else {
                if (value instanceof String) {  // 字符串查询
                    sb.append("and " + key + " like ? ");
                    objects.add(StrUtils.getLikePara(value.toString()));
                }

                if (value instanceof Integer || value instanceof Long) {
                    sb.append("and " + key + " = ? ");
                    objects.add(value);
                }
            }
        }
        sb.append(" order by " + pkName + " " + sort);
        return pageRecord(pageNumber, sb.toString(), objects.toArray());
    }

    /**
     * 查询封装，不分页，返回所有查询到的数据
     * @param params 查询参数
     * @param sort 排序：asc or desc
     * @return
     */
    public List<T> search(Map<String, Object> params, String sort) {
        StringBuilder sb = new StringBuilder("select * from " + tableName + " where 1=1 ");
        List<Object> objects = new ArrayList<Object>();
        Set<String> keySet = params.keySet();
        for (String key : keySet) {
            Object value = params.get(key);

            /**
             * 不等于
             */
            if (key.startsWith("ne_")) {
                key = StringUtils.remove(key, "ne_");
                sb.append("and " + key + " != ? ");
                objects.add(value);
            } else {
                if (value instanceof String) {  // 字符串查询
                    sb.append("and " + key + " like ? ");
                    objects.add(StrUtils.getLikePara(value.toString()));
                }

                if (value instanceof Integer || value instanceof Long) {
                    sb.append("and " + key + " = ? ");
                    objects.add(value);
                }
            }
        }
        sb.append(" order by " + pkName + " " + sort);
        return dao.find(sb.toString(), objects.toArray());
    }

    /**
     * 删除全部
     *
     * @param ids
     */
    public void deleteAll(String ids) {
        for (String idStr : ids.split(",")) {
            dao.deleteById(idStr);
        }
    }
}
