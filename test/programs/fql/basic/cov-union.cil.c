/* Generated by CIL v. 1.3.7 */
/* print_CIL_Input is true */

#line 5 "cov-union.c"
int foo(int x ) 
{ 

  {
#line 7
  if (x == 5) {
    goto switch_0_5;
  } else {
#line 9
    if (x == 6) {
      goto switch_0_6;
    } else {
#line 11
      if (x == 7) {
        goto switch_0_7;
      } else {
        {
        goto switch_0_default;
#line 6
        if (0) {
          switch_0_5: /* CIL Label */ 
#line 8
          return (3);
          switch_0_6: /* CIL Label */ 
#line 10
          return (4);
          switch_0_7: /* CIL Label */ 
#line 12
          return (5);
          switch_0_default: /* CIL Label */ ;
#line 14
          return (6);
        } else {
          switch_0_break: /* CIL Label */ ;
        }
        }
      }
    }
  }
}
}
#line 18 "cov-union.c"
int main(int argc , char **argv ) 
{ int tmp ;

  {
#line 19
  if (argc > 4) {
    {
#line 20
    tmp = foo(argc);
    }
#line 20
    return (tmp);
  } else {

  }
#line 21
  return (0);
}
}
