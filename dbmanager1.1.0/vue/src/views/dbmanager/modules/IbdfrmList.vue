<template>
  <a-card >
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">
          <span style="color:red" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
             备份文件路径：{{backupFilePath}}
            </a-col>
          </span>
        </a-row>
      </a-form-model>
    </div>

    <!-- table区域-begin -->
<!--    <div>-->
<!--      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">-->
<!--        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项-->
<!--        <a style="margin-left: 24px" @click="onClearSelected">清空</a>-->
<!--      </div>-->

      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        class="j-table-force-nowrap"
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>

<!--        <span slot="action" slot-scope="text, record">-->
<!--          <a @click="handleEdit(record)">编辑</a>-->

<!--          <a-divider type="vertical" />-->
<!--          <a-dropdown>-->
<!--            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>-->
<!--            <a-menu slot="overlay">-->
<!--              <a-menu-item>-->
<!--                <a @click="handleDetail(record)">详情</a>-->
<!--              </a-menu-item>-->
<!--              <a-menu-item>-->
<!--                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">-->
<!--                  <a>删除</a>-->
<!--                </a-popconfirm>-->
<!--              </a-menu-item>-->
<!--            </a-menu>-->
<!--          </a-dropdown>-->
<!--        </span>-->

      </a-table>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  // import ProjectModal from './modules/BusinessModal'


  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      // ProjectModal
    },
    data () {
      return {
        description: 'adad管理页面',
        // 表头
        columns: [
          {
            title:'文件名',
            align:"center",
            dataIndex: 'fileName'
          },{
            title:'文件大小',
            align:"center",
            dataIndex: 'fileSize'
          },
        ],
        url: {
          list: "/joblog/getIbdFrm",
          delete: "/business/delete",
          deleteBatch: "/business/deleteBatch"
        },
        dictOptions:{},
        backupFilePath:""
      }
    },
    created() {
    },
    computed: {
      importExcelUrl: function(){
        return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
      },
    },
    methods: {
      initDictConfig(){
      },
      show(record){
        this.url.list = "/joblog/getIbdFrm?id="+record.id
        this.backupFilePath = record.ibdfrmPath
        this.searchQuery();
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>