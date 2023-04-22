package org.start2do.ebean.service;

import io.ebean.DB;
import io.ebean.Model;
import io.ebean.PagedList;
import io.ebean.typequery.TQRootBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.start2do.dto.DataNotFoundException;
import org.start2do.dto.Page;
import org.start2do.ebean.EPage;

public abstract class AbsService<T extends Model> implements IService<T> {

    private final Class<T> aclass = getTClass();

    protected Class getTClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class<T>) actualTypeArguments[0];
    }

    @Override
    public T findOneById(Object id) {
        return DB.find(aclass).setId(id).findOne();
    }

    @Override
    public T findOneByIdUseCache(Object id) {
        return DB.find(aclass, id);
    }

    @Override
    public void update(T entity) {
        DB.update(entity);
    }

    @Override
    public boolean delete(T obj) {
        return obj.delete();
    }

    @Override
    public int deleteById(Object id) {
        return DB.delete(aclass, id);
    }

    @Override
    public int handDeleteById(Object id) {
        return DB.deletePermanent(aclass, id);
    }

    @Override
    public <S extends TQRootBean> boolean handDelete(TQRootBean<T, S> bean) {
        return DB.deletePermanent(bean.findOne());
    }

    @Override
    public void save(T entity) {
        entity.save();
    }

    @Override
    public <S extends TQRootBean> T getOne(TQRootBean<T, S> bean) {
        return bean.findOneOrEmpty().orElseThrow(DataNotFoundException::new);
    }


    @Override
    public T getById(Object id) {
        return Optional.ofNullable(DB.find(aclass).setId(id).findOne()).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public T getByIdUseCache(Object id) {
        return Optional.ofNullable(DB.find(aclass, id)).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public List<T> findAll() {
        return DB.find(aclass).findList();
    }

    @Override
    public <S extends TQRootBean> T findOne(TQRootBean<T, S> bean) {
        return bean.findOne();
    }

    @Override
    public <S extends TQRootBean> T findOneUseCache(TQRootBean<T, S> bean) {
        bean.setUseQueryCache(true);
        return bean.findOne();
    }

    @Override
    public <S extends TQRootBean> List<T> findAll(TQRootBean<T, S> bean) {
        return bean.findList();
    }

    @Override
    public <S extends TQRootBean> List<T> findAllUseCache(TQRootBean<T, S> bean) {
        return bean.findList();
    }

    @Override
    public <S extends TQRootBean> void delete(TQRootBean<T, S> bean) {
        bean.delete();
    }

    @Override
    public <S extends TQRootBean> Page<T> page(TQRootBean<T, S> bean, Page page) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<T>(bean.findPagedList());
    }

    @Override
    public <S extends TQRootBean> Page<T> pageUseCache(TQRootBean<T, S> bean, Page page) {
        bean.setUseQueryCache(true);
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<T>(bean.findPagedList());
    }


    @Override
    public <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<R>(bean.findPagedList(), mapper);
    }

    @Override
    public <S extends TQRootBean, R> Page<R> pageUseCache(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper) {
        bean.setUseQueryCache(true);
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        return new EPage<R>(bean.findPagedList(), mapper);
    }

    @Override
    public <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        if (function != null) {
            function.accept(list.getList());
        }
        return new EPage<>(list, mapper);
    }


    @Override
    public <S extends TQRootBean> int count(TQRootBean<T, S> bean) {
        return bean.findCount();
    }

    @Override
    public <S extends TQRootBean> int countUseCache(TQRootBean<T, S> bean) {
        return bean.findCount();
    }


    @Override
    public <S> boolean exists(TQRootBean<T, S> bean) {
        return bean.exists();
    }

    @Override
    public <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page, Consumer<Collection<T>> function,
        Function<? super T, ? extends R> mapper, Consumer<Collection<R>> function2) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        if (function != null) {
            function.accept(list.getList());
        }
        EPage<R> ePage = new EPage<>(list, mapper);
        if (function2 != null) {
            function2.accept(ePage.getRecords());
        }
        return ePage;
    }

    @Override
    public <S extends TQRootBean, R> Page<R> page(TQRootBean<T, S> bean, Page page,
        Function<? super T, ? extends R> mapper, Runner<T, R> function2) {
        bean.setMaxRows(page.getSize()).setFirstRow(page.getOffset());
        PagedList<T> list = bean.findPagedList();
        EPage<R> ePage = new EPage<>(list, mapper);
        if (function2 != null) {
            function2.run(list.getList(), ePage.getRecords());
        }
        return ePage;
    }

    public interface Runner<T, R> {

        void run(Collection<T> list, Collection<R> resps);
    }

}
