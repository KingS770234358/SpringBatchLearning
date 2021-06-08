package com.wq.bilibilicourse.config.itemreaderl;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;

public class ItemReaderImpl<String> implements ItemReader {

    private Iterator<String> iterator;
    public ItemReaderImpl(List<String> list){
        this.iterator = list.iterator();
    }
    @Override
    public Object read() throws Exception {
        if(iterator.hasNext()){
            return this.iterator.next();
        }
        return null;
    }
}
