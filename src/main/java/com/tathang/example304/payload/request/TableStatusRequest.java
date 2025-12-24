package com.tathang.example304.payload.request;

import com.tathang.example304.model.CoffeeTable;

public class TableStatusRequest {
    private CoffeeTable.TableStatus status;

    // 🆕 THÊM CONSTRUCTOR MẶC ĐỊNH
    public TableStatusRequest() {}
    
    // Constructor có tham số (tùy chọn)
    public TableStatusRequest(CoffeeTable.TableStatus status) {
        this.status = status;
    }

    public CoffeeTable.TableStatus getStatus() {
        return status;
    }

    public void setStatus(CoffeeTable.TableStatus status) {
        this.status = status;
    }
}