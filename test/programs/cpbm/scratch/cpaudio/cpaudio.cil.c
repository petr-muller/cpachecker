/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 211 "/usr/lib/gcc/x86_64-linux-gnu/4.4.3/include/stddef.h"
typedef unsigned long size_t;
#line 92 "cpaudio.c"
struct ushort8_s {
   unsigned short data[8] ;
};
#line 92 "cpaudio.c"
typedef struct ushort8_s ushort8;
#line 203 "../spu_mfcio.h"
extern void mfc_put(void volatile   *ls , unsigned int ea , unsigned int size , unsigned int tag ,
                    unsigned int tid , unsigned int rid ) ;
#line 211
extern void mfc_get(void volatile   *ls , unsigned int ea , unsigned int size , unsigned int tag ,
                    unsigned int tid , unsigned int rid ) ;
#line 252
extern void mfc_write_tag_mask(unsigned int mask ) ;
#line 270
extern void mfc_read_tag_status_all() ;
#line 71 "/usr/include/assert.h"
extern  __attribute__((__nothrow__, __noreturn__)) void __assert_fail(char const   *__assertion ,
                                                                      char const   *__file ,
                                                                      unsigned int __line ,
                                                                      char const   *__function ) ;
#line 98 "cpaudio.c"
ushort8 in[2][1 << 10]  ;
#line 99 "cpaudio.c"
ushort8 out[2][1 << 10]  ;
#line 153 "cpaudio.c"
int spu_main(unsigned long long dst , unsigned long long src , size_t size , char source_channel ) 
{ int i ;
  int n_vec_left ;
  int n_frames ;
  int rem_bytes ;
  unsigned int in_tags ;
  unsigned int out_tags ;
  size_t __cil_tmp16 ;
  unsigned long __cil_tmp17 ;
  unsigned long __cil_tmp18 ;
  unsigned long __cil_tmp19 ;
  unsigned long __cil_tmp20 ;
  ushort8 *__cil_tmp21 ;
  void volatile   *__cil_tmp22 ;
  unsigned int __cil_tmp23 ;
  unsigned long __cil_tmp24 ;
  unsigned int __cil_tmp25 ;
  unsigned long __cil_tmp26 ;
  unsigned long long __cil_tmp27 ;
  int __cil_tmp28 ;
  unsigned long __cil_tmp29 ;
  int __cil_tmp30 ;
  int __cil_tmp31 ;
  unsigned long __cil_tmp32 ;
  unsigned long __cil_tmp33 ;
  unsigned long __cil_tmp34 ;
  ushort8 *__cil_tmp35 ;
  void volatile   *__cil_tmp36 ;
  unsigned int __cil_tmp37 ;
  unsigned long __cil_tmp38 ;
  unsigned int __cil_tmp39 ;
  int __cil_tmp40 ;
  int __cil_tmp41 ;
  unsigned int __cil_tmp42 ;
  unsigned int __cil_tmp43 ;
  unsigned long __cil_tmp44 ;
  unsigned long long __cil_tmp45 ;
  int __cil_tmp46 ;
  unsigned int __cil_tmp47 ;
  unsigned int __cil_tmp48 ;
  int __cil_tmp49 ;
  unsigned int __cil_tmp50 ;
  int __cil_tmp51 ;
  unsigned int __cil_tmp52 ;
  unsigned int __cil_tmp53 ;
  int __cil_tmp54 ;
  unsigned int __cil_tmp55 ;
  unsigned long __cil_tmp56 ;
  int __cil_tmp57 ;
  unsigned long __cil_tmp58 ;
  unsigned long __cil_tmp59 ;
  unsigned long __cil_tmp60 ;
  ushort8 *__cil_tmp61 ;
  void volatile   *__cil_tmp62 ;
  unsigned int __cil_tmp63 ;
  unsigned long __cil_tmp64 ;
  unsigned int __cil_tmp65 ;
  int __cil_tmp66 ;
  unsigned int __cil_tmp67 ;
  unsigned int __cil_tmp68 ;
  unsigned long __cil_tmp69 ;
  unsigned long long __cil_tmp70 ;
  int __cil_tmp71 ;
  int __cil_tmp72 ;
  unsigned long __cil_tmp73 ;
  size_t __cil_tmp74 ;
  unsigned long __cil_tmp75 ;
  unsigned long __cil_tmp76 ;
  int __cil_tmp77 ;
  unsigned long __cil_tmp78 ;
  unsigned long __cil_tmp79 ;
  unsigned long __cil_tmp80 ;
  ushort8 *__cil_tmp81 ;
  void volatile   *__cil_tmp82 ;
  unsigned int __cil_tmp83 ;
  unsigned long __cil_tmp84 ;
  unsigned long __cil_tmp85 ;
  unsigned int __cil_tmp86 ;
  int __cil_tmp87 ;
  unsigned int __cil_tmp88 ;
  unsigned int __cil_tmp89 ;
  unsigned long __cil_tmp90 ;
  unsigned long __cil_tmp91 ;
  unsigned long long __cil_tmp92 ;
  int __cil_tmp93 ;
  int __cil_tmp94 ;
  unsigned int __cil_tmp95 ;
  unsigned int __cil_tmp96 ;
  int __cil_tmp97 ;
  unsigned int __cil_tmp98 ;
  int __cil_tmp99 ;
  int __cil_tmp100 ;
  unsigned int __cil_tmp101 ;
  unsigned int __cil_tmp102 ;
  int __cil_tmp103 ;
  unsigned int __cil_tmp104 ;
  unsigned long __cil_tmp105 ;
  int __cil_tmp106 ;
  int __cil_tmp107 ;
  unsigned long __cil_tmp108 ;
  unsigned long __cil_tmp109 ;
  unsigned long __cil_tmp110 ;
  ushort8 *__cil_tmp111 ;
  void volatile   *__cil_tmp112 ;
  unsigned int __cil_tmp113 ;
  unsigned long __cil_tmp114 ;
  unsigned int __cil_tmp115 ;
  int __cil_tmp116 ;
  int __cil_tmp117 ;
  unsigned int __cil_tmp118 ;
  unsigned int __cil_tmp119 ;
  unsigned long __cil_tmp120 ;
  unsigned long long __cil_tmp121 ;
  int __cil_tmp122 ;
  unsigned int __cil_tmp123 ;
  unsigned int __cil_tmp124 ;
  int __cil_tmp125 ;
  unsigned int __cil_tmp126 ;
  int __cil_tmp127 ;
  unsigned int __cil_tmp128 ;
  unsigned int __cil_tmp129 ;
  int __cil_tmp130 ;
  unsigned int __cil_tmp131 ;
  unsigned long __cil_tmp132 ;
  unsigned long __cil_tmp133 ;
  int __cil_tmp134 ;
  unsigned long __cil_tmp135 ;
  unsigned long __cil_tmp136 ;
  unsigned long __cil_tmp137 ;
  ushort8 *__cil_tmp138 ;
  void volatile   *__cil_tmp139 ;
  unsigned int __cil_tmp140 ;
  unsigned long __cil_tmp141 ;
  unsigned long __cil_tmp142 ;
  unsigned int __cil_tmp143 ;
  unsigned int __cil_tmp144 ;
  unsigned int __cil_tmp145 ;
  int __cil_tmp146 ;
  unsigned int __cil_tmp147 ;

  {
  {
#line 160
  in_tags = 0U;
#line 161
  out_tags = 2U;
#line 174
  __cil_tmp16 = size >> 14;
#line 174
  n_frames = (int )__cil_tmp16;
#line 177
  __cil_tmp17 = 0 * 16UL;
#line 177
  __cil_tmp18 = 0 * 16384UL;
#line 177
  __cil_tmp19 = __cil_tmp18 + __cil_tmp17;
#line 177
  __cil_tmp20 = (unsigned long )(in) + __cil_tmp19;
#line 177
  __cil_tmp21 = (ushort8 *)__cil_tmp20;
#line 177
  __cil_tmp22 = (void volatile   *)__cil_tmp21;
#line 177
  __cil_tmp23 = (unsigned int )src;
#line 177
  __cil_tmp24 = 16UL << 10;
#line 177
  __cil_tmp25 = (unsigned int )__cil_tmp24;
#line 177
  mfc_get(__cil_tmp22, __cil_tmp23, __cil_tmp25, in_tags, 0U, 0U);
#line 178
  __cil_tmp26 = 16UL << 10;
#line 178
  __cil_tmp27 = (unsigned long long )__cil_tmp26;
#line 178
  src = src + __cil_tmp27;
#line 186
  i = 0;
  }
  {
#line 186
  while (1) {
    while_continue: /* CIL Label */ ;
    {
#line 186
    __cil_tmp28 = n_frames - 1;
#line 186
    if (i < __cil_tmp28) {

    } else {
#line 186
      goto while_break;
    }
    }
#line 188
    if (in_tags == 0U) {

    } else {
      {
#line 188
      __assert_fail("(in_tags) == 0", "cpaudio.c", 188U, "spu_main");
      }
    }
#line 189
    if (out_tags == 2U) {

    } else {
      {
#line 189
      __assert_fail("(out_tags) == 2", "cpaudio.c", 189U, "spu_main");
      }
    }
    {
#line 192
    __cil_tmp29 = 0 * 16UL;
#line 192
    __cil_tmp30 = i + 1;
#line 192
    __cil_tmp31 = __cil_tmp30 & 1;
#line 192
    __cil_tmp32 = __cil_tmp31 * 16384UL;
#line 192
    __cil_tmp33 = __cil_tmp32 + __cil_tmp29;
#line 192
    __cil_tmp34 = (unsigned long )(in) + __cil_tmp33;
#line 192
    __cil_tmp35 = (ushort8 *)__cil_tmp34;
#line 192
    __cil_tmp36 = (void volatile   *)__cil_tmp35;
#line 192
    __cil_tmp37 = (unsigned int )src;
#line 192
    __cil_tmp38 = 16UL << 10;
#line 192
    __cil_tmp39 = (unsigned int )__cil_tmp38;
#line 192
    __cil_tmp40 = i + 1;
#line 192
    __cil_tmp41 = __cil_tmp40 & 1;
#line 192
    __cil_tmp42 = (unsigned int )__cil_tmp41;
#line 192
    __cil_tmp43 = in_tags + __cil_tmp42;
#line 192
    mfc_get(__cil_tmp36, __cil_tmp37, __cil_tmp39, __cil_tmp43, 0U, 0U);
#line 193
    __cil_tmp44 = 16UL << 10;
#line 193
    __cil_tmp45 = (unsigned long long )__cil_tmp44;
#line 193
    src = src + __cil_tmp45;
#line 196
    __cil_tmp46 = i & 1;
#line 196
    __cil_tmp47 = (unsigned int )__cil_tmp46;
#line 196
    __cil_tmp48 = in_tags + __cil_tmp47;
#line 196
    __cil_tmp49 = 1 << __cil_tmp48;
#line 196
    __cil_tmp50 = (unsigned int )__cil_tmp49;
#line 196
    mfc_write_tag_mask(__cil_tmp50);
#line 197
    mfc_read_tag_status_all();
#line 200
    __cil_tmp51 = i & 1;
#line 200
    __cil_tmp52 = (unsigned int )__cil_tmp51;
#line 200
    __cil_tmp53 = out_tags + __cil_tmp52;
#line 200
    __cil_tmp54 = 1 << __cil_tmp53;
#line 200
    __cil_tmp55 = (unsigned int )__cil_tmp54;
#line 200
    mfc_write_tag_mask(__cil_tmp55);
#line 201
    mfc_read_tag_status_all();
#line 209
    __cil_tmp56 = 0 * 16UL;
#line 209
    __cil_tmp57 = i & 1;
#line 209
    __cil_tmp58 = __cil_tmp57 * 16384UL;
#line 209
    __cil_tmp59 = __cil_tmp58 + __cil_tmp56;
#line 209
    __cil_tmp60 = (unsigned long )(out) + __cil_tmp59;
#line 209
    __cil_tmp61 = (ushort8 *)__cil_tmp60;
#line 209
    __cil_tmp62 = (void volatile   *)__cil_tmp61;
#line 209
    __cil_tmp63 = (unsigned int )dst;
#line 209
    __cil_tmp64 = 16UL << 10;
#line 209
    __cil_tmp65 = (unsigned int )__cil_tmp64;
#line 209
    __cil_tmp66 = i & 1;
#line 209
    __cil_tmp67 = (unsigned int )__cil_tmp66;
#line 209
    __cil_tmp68 = out_tags + __cil_tmp67;
#line 209
    mfc_put(__cil_tmp62, __cil_tmp63, __cil_tmp65, __cil_tmp68, 0U, 0U);
#line 210
    __cil_tmp69 = 16UL << 10;
#line 210
    __cil_tmp70 = (unsigned long long )__cil_tmp69;
#line 210
    dst = dst + __cil_tmp70;
#line 186
    i = i + 1;
    }
  }
  while_break: /* CIL Label */ ;
  }
  {
#line 215
  __cil_tmp71 = 1 << 10;
#line 215
  __cil_tmp72 = __cil_tmp71 - 1;
#line 215
  __cil_tmp73 = (unsigned long )__cil_tmp72;
#line 215
  __cil_tmp74 = size >> 4;
#line 215
  __cil_tmp75 = __cil_tmp74 & __cil_tmp73;
#line 215
  n_vec_left = (int )__cil_tmp75;
#line 218
  __cil_tmp76 = 0 * 16UL;
#line 218
  __cil_tmp77 = n_frames & 1;
#line 218
  __cil_tmp78 = __cil_tmp77 * 16384UL;
#line 218
  __cil_tmp79 = __cil_tmp78 + __cil_tmp76;
#line 218
  __cil_tmp80 = (unsigned long )(in) + __cil_tmp79;
#line 218
  __cil_tmp81 = (ushort8 *)__cil_tmp80;
#line 218
  __cil_tmp82 = (void volatile   *)__cil_tmp81;
#line 218
  __cil_tmp83 = (unsigned int )src;
#line 218
  __cil_tmp84 = (unsigned long )n_vec_left;
#line 218
  __cil_tmp85 = 16UL * __cil_tmp84;
#line 218
  __cil_tmp86 = (unsigned int )__cil_tmp85;
#line 218
  __cil_tmp87 = n_frames & 1;
#line 218
  __cil_tmp88 = (unsigned int )__cil_tmp87;
#line 218
  __cil_tmp89 = in_tags + __cil_tmp88;
#line 218
  mfc_get(__cil_tmp82, __cil_tmp83, __cil_tmp86, __cil_tmp89, 0U, 0U);
#line 219
  __cil_tmp90 = (unsigned long )n_vec_left;
#line 219
  __cil_tmp91 = 16UL * __cil_tmp90;
#line 219
  __cil_tmp92 = (unsigned long long )__cil_tmp91;
#line 219
  src = src + __cil_tmp92;
#line 222
  __cil_tmp93 = n_frames - 1;
#line 222
  __cil_tmp94 = __cil_tmp93 & 1;
#line 222
  __cil_tmp95 = (unsigned int )__cil_tmp94;
#line 222
  __cil_tmp96 = in_tags + __cil_tmp95;
#line 222
  __cil_tmp97 = 1 << __cil_tmp96;
#line 222
  __cil_tmp98 = (unsigned int )__cil_tmp97;
#line 222
  mfc_write_tag_mask(__cil_tmp98);
#line 223
  mfc_read_tag_status_all();
#line 226
  __cil_tmp99 = n_frames - 1;
#line 226
  __cil_tmp100 = __cil_tmp99 & 1;
#line 226
  __cil_tmp101 = (unsigned int )__cil_tmp100;
#line 226
  __cil_tmp102 = out_tags + __cil_tmp101;
#line 226
  __cil_tmp103 = 1 << __cil_tmp102;
#line 226
  __cil_tmp104 = (unsigned int )__cil_tmp103;
#line 226
  mfc_write_tag_mask(__cil_tmp104);
#line 227
  mfc_read_tag_status_all();
#line 235
  __cil_tmp105 = 0 * 16UL;
#line 235
  __cil_tmp106 = n_frames - 1;
#line 235
  __cil_tmp107 = __cil_tmp106 & 1;
#line 235
  __cil_tmp108 = __cil_tmp107 * 16384UL;
#line 235
  __cil_tmp109 = __cil_tmp108 + __cil_tmp105;
#line 235
  __cil_tmp110 = (unsigned long )(out) + __cil_tmp109;
#line 235
  __cil_tmp111 = (ushort8 *)__cil_tmp110;
#line 235
  __cil_tmp112 = (void volatile   *)__cil_tmp111;
#line 235
  __cil_tmp113 = (unsigned int )dst;
#line 235
  __cil_tmp114 = 16UL << 10;
#line 235
  __cil_tmp115 = (unsigned int )__cil_tmp114;
#line 235
  __cil_tmp116 = n_frames - 1;
#line 235
  __cil_tmp117 = __cil_tmp116 & 1;
#line 235
  __cil_tmp118 = (unsigned int )__cil_tmp117;
#line 235
  __cil_tmp119 = out_tags + __cil_tmp118;
#line 235
  mfc_put(__cil_tmp112, __cil_tmp113, __cil_tmp115, __cil_tmp119, 0U, 0U);
#line 236
  __cil_tmp120 = 16UL << 10;
#line 236
  __cil_tmp121 = (unsigned long long )__cil_tmp120;
#line 236
  dst = dst + __cil_tmp121;
#line 241
  __cil_tmp122 = n_frames & 1;
#line 241
  __cil_tmp123 = (unsigned int )__cil_tmp122;
#line 241
  __cil_tmp124 = in_tags + __cil_tmp123;
#line 241
  __cil_tmp125 = 1 << __cil_tmp124;
#line 241
  __cil_tmp126 = (unsigned int )__cil_tmp125;
#line 241
  mfc_write_tag_mask(__cil_tmp126);
#line 242
  mfc_read_tag_status_all();
#line 245
  __cil_tmp127 = n_frames & 1;
#line 245
  __cil_tmp128 = (unsigned int )__cil_tmp127;
#line 245
  __cil_tmp129 = out_tags + __cil_tmp128;
#line 245
  __cil_tmp130 = 1 << __cil_tmp129;
#line 245
  __cil_tmp131 = (unsigned int )__cil_tmp130;
#line 245
  mfc_write_tag_mask(__cil_tmp131);
#line 246
  mfc_read_tag_status_all();
#line 259
  __cil_tmp132 = size & 15UL;
#line 259
  rem_bytes = (int )__cil_tmp132;
  }
#line 260
  if (rem_bytes) {
#line 267
    n_vec_left = n_vec_left + 1;
  } else {

  }
  {
#line 271
  __cil_tmp133 = 0 * 16UL;
#line 271
  __cil_tmp134 = n_frames & 1;
#line 271
  __cil_tmp135 = __cil_tmp134 * 16384UL;
#line 271
  __cil_tmp136 = __cil_tmp135 + __cil_tmp133;
#line 271
  __cil_tmp137 = (unsigned long )(out) + __cil_tmp136;
#line 271
  __cil_tmp138 = (ushort8 *)__cil_tmp137;
#line 271
  __cil_tmp139 = (void volatile   *)__cil_tmp138;
#line 271
  __cil_tmp140 = (unsigned int )dst;
#line 271
  __cil_tmp141 = (unsigned long )n_vec_left;
#line 271
  __cil_tmp142 = 16UL * __cil_tmp141;
#line 271
  __cil_tmp143 = (unsigned int )__cil_tmp142;
#line 271
  __cil_tmp144 = out_tags + 1U;
#line 271
  mfc_put(__cil_tmp139, __cil_tmp140, __cil_tmp143, __cil_tmp144, 0U, 0U);
#line 274
  __cil_tmp145 = out_tags + 1U;
#line 274
  __cil_tmp146 = 1 << __cil_tmp145;
#line 274
  __cil_tmp147 = (unsigned int )__cil_tmp146;
#line 274
  mfc_write_tag_mask(__cil_tmp147);
#line 275
  mfc_read_tag_status_all();
  }
#line 276
  return (0);
}
}
