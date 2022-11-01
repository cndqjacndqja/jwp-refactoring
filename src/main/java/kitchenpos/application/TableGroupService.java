package kitchenpos.application;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.application.request.TableGroupRequest;
import kitchenpos.application.response.TableGroupResponse;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.OrderTables;
import kitchenpos.domain.TableGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TableGroupService {
    private final OrderDao orderDao;
    private final OrderTableDao orderTableDao;
    private final TableGroupDao tableGroupDao;

    public TableGroupService(final OrderDao orderDao, final OrderTableDao orderTableDao,
                             final TableGroupDao tableGroupDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
        this.tableGroupDao = tableGroupDao;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest request) {
        List<Long> orderTableIds = request.getOrderTableIds();
        final List<OrderTable> savedOrderTables = getOrderTables(orderTableIds);
        return createTableGroup(savedOrderTables);
    }

    private List<OrderTable> getOrderTables(final List<Long> orderTableIds) {
        OrderTables orderTables = new OrderTables(orderTableDao.findAllByIdIn(orderTableIds));
        if (!orderTables.matchSize(orderTableIds.size())) {
            throw new IllegalArgumentException("주문 정보가 실제 주문한 정보와 일치하지 않습니다.");
        }
        return orderTables.getOrderTables();
    }

    private TableGroupResponse createTableGroup(final List<OrderTable> orderTables) {
        final TableGroup tableGroup = tableGroupDao.save(new TableGroup());
        final Long tableGroupId = tableGroup.getId();
        return TableGroupResponse.of(tableGroup,
                orderTables.stream()
                        .map(it -> new OrderTable(tableGroupId, it.getNumberOfGuests(), false))
                        .collect(Collectors.toList()));
    }

    private List<Long> getOrderTableIds(final List<OrderTable> orderTables) {
        return orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final List<OrderTable> orderTables = orderTableDao.findAllByTableGroupId(tableGroupId);
        final List<Long> orderTableIds = getOrderTableIds(orderTables);

        if (orderDao.existsByOrderTableIdInAndOrderStatusIn(
                orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
            throw new IllegalArgumentException("조리 중이거나 식사 중인 테이블이 존재합니다.");
        }

        savedUnGroupOrderTables(orderTables);
    }

    private void savedUnGroupOrderTables(final List<OrderTable> orderTables) {
        List<OrderTable> unGroupOrderTables = orderTables.stream()
                .map(it -> new OrderTable(null, it.getNumberOfGuests(), false))
                .collect(Collectors.toList());
        unGroupOrderTables.forEach(orderTableDao::save);
    }
}
