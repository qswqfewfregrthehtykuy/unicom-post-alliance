package com.unicom.post.modules.commission.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.PageResult;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.commission.dto.CommissionDetailQueryDTO;
import com.unicom.post.modules.commission.vo.CommissionDetailVO;
import com.unicom.post.modules.commission.vo.SummaryVO;
import com.unicom.post.modules.commission.mapper.CommissionDetailMapper;
import com.unicom.post.modules.commission.service.CommissionDetailService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionDetailServiceImpl implements CommissionDetailService {

    private final CommissionDetailMapper commissionDetailMapper;
    private final SysUserService userService;

    @Override
    public PageResult<CommissionDetailVO> listCommissionDetails(CommissionDetailQueryDTO query) {
        SysUser currentUser = getCurrentUser();
        Page<CommissionDetailVO> page = new Page<>(query.getPageNo(), query.getPageSize());
        Page<CommissionDetailVO> resultPage = commissionDetailMapper.selectCommissionDetailPage(page, query, currentUser);
        return new PageResult<>(resultPage.getTotal(), query.getPageNo(), query.getPageSize(), resultPage.getRecords());
    }

    @Override
    public SummaryVO summaryStatistics(CommissionDetailQueryDTO query) {
        SysUser currentUser = getCurrentUser();

        // 1. 基础汇总
        CommissionDetailMapper.SummaryBaseData base = commissionDetailMapper.selectSummaryBaseData(query, currentUser);
        if (base == null) {
            base = new CommissionDetailMapper.SummaryBaseData();
            base.totalAmount = BigDecimal.ZERO;
            base.leadTotalAmount = BigDecimal.ZERO;
            base.formalTotalAmount = BigDecimal.ZERO;
            base.settledAmount = BigDecimal.ZERO;
            base.pendingAmount = BigDecimal.ZERO;
            base.frozenAmount = BigDecimal.ZERO;
            base.orderCount = 0;
            base.detailCount = 0;
        }

        // 2. byPayee
        List<CommissionDetailMapper.ByPayeeItem> byPayeeList = commissionDetailMapper.selectByPayeeGroup(query, currentUser);
        List<SummaryVO.ByPayeeItem> byPayee = byPayeeList.stream()
                .map(item -> {
                    SummaryVO.ByPayeeItem vo = new SummaryVO.ByPayeeItem();
                    vo.setPayeeType(item.payeeType);
                    vo.setPayeeCount(item.payeeCount);
                    vo.setTotalAmount(item.totalAmount);
                    vo.setSettledAmount(item.settledAmount);
                    return vo;
                }).collect(Collectors.toList());

        // 3. byPhase
        List<CommissionDetailMapper.ByPhaseItem> byPhaseList = commissionDetailMapper.selectByPhaseGroup(query, currentUser);
        List<SummaryVO.ByPhaseItem> byPhase = byPhaseList.stream()
                .map(item -> {
                    SummaryVO.ByPhaseItem vo = new SummaryVO.ByPhaseItem();
                    vo.setPhase(item.phase);
                    vo.setAmount(item.amount);
                    vo.setSettledAmount(item.settledAmount);
                    return vo;
                }).collect(Collectors.toList());

        SummaryVO result = new SummaryVO();
        result.setTotalAmount(base.totalAmount);
        result.setLeadTotalAmount(base.leadTotalAmount);
        result.setFormalTotalAmount(base.formalTotalAmount);
        result.setSettledAmount(base.settledAmount);
        result.setPendingAmount(base.pendingAmount);
        result.setFrozenAmount(base.frozenAmount);
        result.setOrderCount(base.orderCount);
        result.setDetailCount(base.detailCount);

        SummaryVO.Breakdown breakdown = new SummaryVO.Breakdown();
        breakdown.setByPayee(byPayee);
        breakdown.setByPhase(byPhase);
        result.setBreakdown(breakdown);

        return result;
    }

    @Override
    public void exportCommissionDetails(CommissionDetailQueryDTO query, HttpServletResponse response) {
        // 导出时不分页，取全部数据
        query.setPageNo(1);
        query.setPageSize(Integer.MAX_VALUE);
        PageResult<CommissionDetailVO> pageResult = listCommissionDetails(query);
        List<CommissionDetailVO> list = pageResult.getList();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("佣金明细");
            // 创建表头
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "订单号", "阶段", "收款方类型", "收款方名称", "规则快照", "业务类型快照", "发展来源快照",
                    "总金额", "分成比例", "实得金额", "状态", "结算时间", "创建时间"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(getHeaderStyle(workbook));
            }

            // 填充数据
            int rowIdx = 1;
            for (CommissionDetailVO vo : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(vo.getId());
                row.createCell(1).setCellValue(vo.getOrderNo());
                row.createCell(2).setCellValue(vo.getCommissionPhase());
                row.createCell(3).setCellValue(vo.getPayeeType());
                row.createCell(4).setCellValue(vo.getPayeeName());
                row.createCell(5).setCellValue(vo.getRuleNameSnapshot());
                row.createCell(6).setCellValue(vo.getBusinessTypeSnapshot());
                row.createCell(7).setCellValue(vo.getDevelopSourceSnapshot());
                row.createCell(8).setCellValue(vo.getTotalAmount() != null ? vo.getTotalAmount().doubleValue() : 0);
                row.createCell(9).setCellValue(vo.getRatio() != null ? vo.getRatio().doubleValue() : 0);
                row.createCell(10).setCellValue(vo.getAmount() != null ? vo.getAmount().doubleValue() : 0);
                row.createCell(11).setCellValue(vo.getStatus());
                row.createCell(12).setCellValue(vo.getSettleAt() != null ? vo.getSettleAt().toString() : "");
                row.createCell(13).setCellValue(vo.getCreatedAt() != null ? vo.getCreatedAt().toString() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出文件
            String fileName = "commission-details-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BusinessException(500, "导出Excel失败：" + e.getMessage());
        }
    }

    // ---------- 私有方法 ----------
    private SysUser getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}