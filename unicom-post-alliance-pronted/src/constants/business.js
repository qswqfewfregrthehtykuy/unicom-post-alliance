/**
 * 业务类型常量 — 全项目统一
 * 新增业务、佣金规则、审核筛选、统计报表均引用此文件
 */
export const BUSINESS_TYPES = [
  { value: 'MOBILE_CARD', label: '号卡' },
  { value: 'BROADBAND',   label: '宽带' },
  { value: 'SMART_HOME',  label: '智慧家庭' },
  { value: 'IPTV',        label: 'IPTV' },
  { value: 'OTHER',       label: '其他' }
]

/** 根据 value 获取中文标签 */
export function getBusinessTypeLabel(value) {
  const found = BUSINESS_TYPES.find(item => item.value === value)
  return found ? found.label : (value || '未知')
}

/**
 * 发展来源常量 — 全项目统一
 */
export const DEVELOP_SOURCES = [
  { value: 'SITE_USER',   label: '发展人发展' },
  { value: 'OUTLET_SELF', label: '网点自发展' },
  { value: 'CHANNEL',     label: '渠道合作' },
  { value: 'ONLINE',      label: '线上推广' },
  { value: 'OTHER',       label: '其他' }
]

/** 根据 value 获取中文标签 */
export function getDevelopSourceLabel(value) {
  const found = DEVELOP_SOURCES.find(item => item.value === value)
  return found ? found.label : (value || '未知')
}
