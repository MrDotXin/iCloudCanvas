declare namespace API {
  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListPictureVO_ = {
    code?: number
    data?: PictureVO[]
    message?: string
  }

  type BaseResponseListSpaceLevelInfo_ = {
    code?: number
    data?: SpaceLevelInfo[]
    message?: string
  }

  type BaseResponseListString_ = {
    code?: number
    data?: string[]
    message?: string
  }

  type BaseResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponsePagePicture_ = {
    code?: number
    data?: PagePicture_
    message?: string
  }

  type BaseResponsePagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    message?: string
  }

  type BaseResponsePageUser_ = {
    code?: number
    data?: PageUser_
    message?: string
  }

  type BaseResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BaseResponsePicture_ = {
    code?: number
    data?: Picture
    message?: string
  }

  type BaseResponsePictureTagCategory_ = {
    code?: number
    data?: PictureTagCategory
    message?: string
  }

  type BaseResponsePictureVO_ = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type BaseResponseSpaceVO_ = {
    code?: number
    data?: SpaceVO
    message?: string
  }

  type BaseResponseString_ = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type DeleteRequest = {
    id?: string
  }

  type getPictureByIdUsingGETParams = {
    /** id */
    id: string
  }

  type getPictureVOByIdUsingGETParams = {
    /** id */
    id: string
  }

  type getSpaceVOByUserIdUsingGETParams = {
    /** userId */
    userId: string
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: string
  }

  type LoginUserVO = {
    createTime?: string
    id?: string
    updateTime?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type OrderItem = {
    asc?: boolean
    column?: string
  }

  type PagePicture_ = {
    countId?: string
    current?: string
    maxLimit?: string
    optimizeCountSql?: boolean
    orders?: OrderItem[]
    pages?: string
    records?: Picture[]
    searchCount?: boolean
    size?: string
    total?: string
  }

  type PagePictureVO_ = {
    countId?: string
    current?: string
    maxLimit?: string
    optimizeCountSql?: boolean
    orders?: OrderItem[]
    pages?: string
    records?: PictureVO[]
    searchCount?: boolean
    size?: string
    total?: string
  }

  type PageUser_ = {
    countId?: string
    current?: string
    maxLimit?: string
    optimizeCountSql?: boolean
    orders?: OrderItem[]
    pages?: string
    records?: User[]
    searchCount?: boolean
    size?: string
    total?: string
  }

  type PageUserVO_ = {
    countId?: string
    current?: string
    maxLimit?: string
    optimizeCountSql?: boolean
    orders?: OrderItem[]
    pages?: string
    records?: UserVO[]
    searchCount?: boolean
    size?: string
    total?: string
  }

  type Picture = {
    category?: string
    createTime?: string
    editTime?: string
    id?: string
    introduction?: string
    isDelete?: number
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: string
    picWidth?: number
    rawFormat?: string
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: string
    spaceId?: string
    tags?: string
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    userId?: string
  }

  type PictureEditRequest = {
    category?: string
    id?: string
    introduction?: string
    name?: string
    spaceId?: string
    tags?: string[]
  }

  type PictureQueryRequest = {
    category?: string
    current?: number
    editTimeBegin?: string
    editTimeEnd?: string
    id?: string
    introduction?: string
    name?: string
    pageSize?: number
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: string
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewerId?: string
    searchText?: string
    sortField?: string
    sortOrder?: string
    spaceId?: string
    tags?: string[]
    userId?: string
  }

  type PictureReviewRequest = {
    id?: string
    reviewMessage?: string
    reviewStatus?: number
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUpdateRequest = {
    category?: string
    id?: string
    introduction?: string
    name?: string
    spaceId?: string
    tags?: string[]
  }

  type PictureUploadBatchRequest = {
    count?: number
    picPrefix?: string
    searchText?: string
    spaceId?: string
    start?: number
  }

  type PictureVO = {
    category?: string
    createTime?: string
    editTime?: string
    id?: string
    introduction?: string
    name?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: string
    picWidth?: number
    rawFormat?: string
    spaceId?: string
    tags?: string[]
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    user?: UserVO
    userId?: string
  }

  type SpaceAddRequest = {
    spaceLevel?: number
    spaceName?: string
  }

  type SpaceEditRequest = {
    spaceName?: string
    spaceType?: string
  }

  type SpaceLevelInfo = {
    levelDescription?: string
    levelMaxCount?: string
    levelMaxSize?: string
    levelName?: string
    value?: number
  }

  type SpaceTypeModifyRequest = {
    spaceId?: string
    spaceType?: number
  }

  type SpaceUpdateRequest = {
    maxCount?: string
    maxSize?: string
    spaceId?: string
    spaceLevel?: number
    spaceName?: string
    spaceType?: string
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: string
    isDelete?: number
    maxCount?: string
    maxSize?: string
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    totalCount?: string
    totalSize?: string
    updateTime?: string
    userId?: string
    userVO?: UserVO
  }

  type testDownloadFileUsingGETParams = {
    /** filePath */
    filePath?: string
  }

  type uploadPictureByUrlUsingPOSTParams = {
    id?: string
    picName?: string
    spaceId?: string
    uploadType?: string
    /** url */
    url: string
  }

  type uploadPictureUsingPOSTParams = {
    id?: string
    picName?: string
    spaceId?: string
    uploadType?: string
  }

  type User = {
    createTime?: string
    id?: string
    isDelete?: number
    mpOpenId?: string
    unionId?: string
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserAddRequest = {
    userAccount?: string
    userAvatar?: string
    userName?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    id?: string
    mpOpenId?: string
    pageSize?: number
    sortField?: string
    sortOrder?: string
    unionId?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    checkPassword?: string
    userAccount?: string
    userPassword?: string
  }

  type UserUpdateMyRequest = {
    userAvatar?: string
    userName?: string
    userProfile?: string
  }

  type UserUpdateRequest = {
    id?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
